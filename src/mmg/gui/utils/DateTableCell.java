package mmg.gui.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

public class DateTableCell<T> extends TableCell<T, Date> {

	private final SimpleDateFormat formatter ;
	private final DatePicker datePicker ;

	public DateTableCell() {

		formatter = new SimpleDateFormat("dd/MM/yyyy");
		datePicker = new DatePicker() ;

		// Commit edit on Enter and cancel on Escape.
		// Note that the default behavior consumes key events, so we must 
		// register this as an event filter to capture it.
		// Consequently, with Enter, the datePicker's value won't yet have been updated, 
		// so commit will sent the wrong value. So we must update it ourselves from the
		// editor's text value.

		datePicker.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
			if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
				datePicker.setValue(datePicker.getConverter().fromString(datePicker.getEditor().getText()));
				Date date = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
				commitEdit(  date);
			}
			if (event.getCode() == KeyCode.ESCAPE) {
				cancelEdit();
			}
		});

		// Modify default mouse behavior on date picker:
		// Don't hide popup on single click, just set date
		// On double-click, hide popup and commit edit for editor
		// Must consume event to prevent default hiding behavior, so
		// must update date picker value ourselves.

		// Modify key behavior so that enter on a selected cell commits the edit
		// on that cell's date.
	datePicker.setConverter(new StringConverter<LocalDate>(){

		@Override
		public LocalDate fromString(String sd) {		
			try {
				Date item =  formatter.parse(sd);
				if(item==null)return null;
				Instant instant = Instant.ofEpochMilli(item.getTime());
				LocalDate res = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
				return res;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
		}

		@Override
		public String toString(LocalDate ld) {
			Instant instant = ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
			Date res1 = Date.from(instant);
			return formatter.format(res1);
		}
		
	});
		datePicker.setDayCellFactory(picker -> {
			DateCell dateCell = new DateCell();
			
			dateCell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {

				LocalDate res = getLocalDate();
				
				datePicker.setValue(res);
				if (event.getClickCount() == 2) {
					datePicker.hide();
					LocalDate date = dateCell.getItem();//.atStartOfDay(ZoneId.systemDefault()).toInstant());

					Instant instant = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
					Date res1 = Date.from(instant);
					commitEdit(res1);//MonthDay.from(cell.getItem()));
				}
				event.consume();
			});
			dateCell.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
				if (event.getCode() == KeyCode.ENTER) {
					Date date = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
					commitEdit(date);//Date.from(datePicker.getValue()));
				}
			});
			return dateCell ;
		});

		contentDisplayProperty().bind(Bindings.when(editingProperty())
				.then(ContentDisplay.GRAPHIC_ONLY)
				.otherwise(ContentDisplay.TEXT_ONLY));
	}

	private LocalDate getLocalDate() {
		Date item = this.getItem();
		if(item==null)return null;
		Instant instant = Instant.ofEpochMilli(item.getTime());
		LocalDate res = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
		return res;
	}

	@Override
	public void updateItem(Date birthday, boolean empty) {
		super.updateItem(birthday, empty);
		try{
		if (empty || birthday==null) {
			setText(null);
			setGraphic(null);
		} else {
			String stringDate = formatter.format(birthday);
			setText(stringDate);
			setGraphic(datePicker);
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void startEdit() {
		super.startEdit();
		if (!isEmpty()) {
			datePicker.setValue(getLocalDate());//getItem().atYear(LocalDate.now().getYear()
		}
	}
}