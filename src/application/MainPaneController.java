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

	private String salesforceJdbcUrl = <環境に合わせて設定してください>;
	
	private String kintoneJdbcUrl = <環境に合わせて設定してください>;
	
	@FXML
	private TableView<Contact> salesforceTable;
	
	@FXML
	private TableView<顧客リスト> kintoneTable;
	
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
				
				顧客リスト kintoneItem = kintoneTable.getItems().stream()
					.filter(o -> o.getメールアドレス().equals(contact.getEmail()))
					.findFirst()
					.orElse(new 顧客リスト());
				
				PreparedStatement pstmt;
				
				if (kintoneItem.getRecordId() == 0) {
					pstmt = conn.prepareStatement("INSERT INTO 顧客リスト(メールアドレス, 会社名, 住所, 担当者名, 部署名, [郵便番号(数字のみ)], [Tel(数字のみ)], [Fax(数字のみ)]) VALUES(?, ? ,? ,? ,? ,? ,? ,?)");
				} else {
					pstmt = conn.prepareStatement("UPDATE 顧客リスト SET メールアドレス=?, 会社名=?, 住所=?, 担当者名=?, 部署名=?, [郵便番号(数字のみ)]=?, [Tel(数字のみ)]=?, [Fax(数字のみ)]=? WHERE RecordId=?");
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
			PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM 顧客リスト");
			ResultSet rs = pstmt.executeQuery();
			) {

			while (rs.next()) {
				顧客リスト c = new 顧客リスト();
				c.setRecordId(rs.getInt("RecordId"));
				c.setFax_数字のみ_(rs.getString("Fax(数字のみ)"));
				c.setTel_数字のみ_(rs.getString("Tel(数字のみ)"));
				c.setメールアドレス(rs.getString("メールアドレス"));
				c.setレコード番号(rs.getString("レコード番号"));
				c.set会社名(rs.getString("会社名"));
				c.set住所(rs.getString("住所"));
				c.set担当者名(rs.getString("担当者名"));
				c.set部署名(rs.getString("部署名"));
				c.set郵便番号_数字のみ_(rs.getString("郵便番号(数字のみ)"));
				kintoneTable.getItems().add(c);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
}
