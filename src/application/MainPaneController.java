package application;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;


public class MainPaneController implements Initializable {

	private String salesforceJdbcUrl = <���ɍ��킹�Đݒ肵�Ă�������>;
	
	private String kintoneJdbcUrl = <���ɍ��킹�Đݒ肵�Ă�������>;
	
	@FXML
	private TableView<Contact> salesforceTable;
	
	@FXML
	private TableView<�ڋq���X�g> kintoneTable;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		salesforceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		loadSalesforceTableView();
		loadKintoneTableView();
	}
	
	@FXML
	private void importOnClick(MouseEvent event) {
		
		try (Connection conn = DriverManager.getConnection(kintoneJdbcUrl)) {
			
			conn.setAutoCommit(false);
			
			for (Contact contact : salesforceTable.getSelectionModel().getSelectedItems()) {
				
				�ڋq���X�g kintoneItem = kintoneTable.getItems().stream()
					.filter(o -> o.get���[���A�h���X().equals(contact.getEmail()))
					.findFirst()
					.orElse(new �ڋq���X�g());
				
				PreparedStatement pstmt;
				
				if (kintoneItem.getRecordId() == 0) {
					pstmt = conn.prepareStatement("INSERT INTO �ڋq���X�g(���[���A�h���X, ��Ж�, �Z��, �S���Җ�, ������, [�X�֔ԍ�(�����̂�)], [Tel(�����̂�)], [Fax(�����̂�)]) VALUES(?, ? ,? ,? ,? ,? ,? ,?)");
				} else {
					pstmt = conn.prepareStatement("UPDATE �ڋq���X�g SET ���[���A�h���X=?, ��Ж�=?, �Z��=?, �S���Җ�=?, ������=?, [�X�֔ԍ�(�����̂�)]=?, [Tel(�����̂�)]=?, [Fax(�����̂�)]=? WHERE RecordId=?");
					pstmt.setInt(9, kintoneItem.getRecordId());
				}
				
				pstmt.setString(1, contact.getEmail());
				pstmt.setString(2, contact.getAccountName());
				pstmt.setString(3, contact.getAccountAddress());
				pstmt.setString(4, contact.getName());
				pstmt.setString(5, Optional.ofNullable(contact.getDepartment()).orElse(""));
				pstmt.setString(6, Optional.ofNullable(contact.getAccountBillingPostalCode()).orElse("").replace("-", ""));
				pstmt.setString(7, contact.getPhone());
				pstmt.setString(8, contact.getFax());
				
				pstmt.execute();
				pstmt.close();
			}
			
			conn.commit();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		loadKintoneTableView();
	}
	
	private void loadSalesforceTableView() {
		
		salesforceTable.getItems().clear();
		
		try (
			Connection conn = DriverManager.getConnection(salesforceJdbcUrl);
			PreparedStatement pstmt = conn.prepareStatement(
				"SELECT c.*, " +
				"a.Name AccountName, a.BillingPostalCode AccountBillingPostalCode, CONCAT(a.BillingState, a.BillingCity, a.BillingStreet) AccountAddress " +
				"FROM Contact c INNER JOIN Account a ON a.Id=c.AccountId");
			ResultSet rs = pstmt.executeQuery();
			) {

			while (rs.next()) {
				Contact c = new Contact();
				c.setId(rs.getString("Id"));
				c.setAccountId(rs.getString("AccountId"));
				c.setDepartment(rs.getString("Department"));
				c.setEmail(rs.getString("Email"));
				c.setFax(rs.getString("Fax"));
				c.setName(rs.getString("Name"));
				c.setPhone(rs.getString("Phone"));
				c.setAccountName(rs.getString("AccountName"));
				c.setAccountAddress(rs.getString("AccountAddress"));
				c.setAccountBillingPostalCode(rs.getString("AccountBillingPostalCode"));
				salesforceTable.getItems().add(c);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void loadKintoneTableView() {
	
		kintoneTable.getItems().clear();
		
		try (
			Connection conn = DriverManager.getConnection(kintoneJdbcUrl);
			PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM �ڋq���X�g");
			ResultSet rs = pstmt.executeQuery();
			) {

			while (rs.next()) {
				�ڋq���X�g c = new �ڋq���X�g();
				c.setRecordId(rs.getInt("RecordId"));
				c.setFax_�����̂�_(rs.getString("Fax(�����̂�)"));
				c.setTel_�����̂�_(rs.getString("Tel(�����̂�)"));
				c.set���[���A�h���X(rs.getString("���[���A�h���X"));
				c.set���R�[�h�ԍ�(rs.getString("���R�[�h�ԍ�"));
				c.set��Ж�(rs.getString("��Ж�"));
				c.set�Z��(rs.getString("�Z��"));
				c.set�S���Җ�(rs.getString("�S���Җ�"));
				c.set������(rs.getString("������"));
				c.set�X�֔ԍ�_�����̂�_(rs.getString("�X�֔ԍ�(�����̂�)"));
				kintoneTable.getItems().add(c);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
}
