/**
 * 
 */
package com.example.demo.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Robert Tu - Jul 17, 2016
 *
 */

@Entity
@Table(name="books")
public class Book {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String title;
	private String author;
	private String description;
	
	public Book() {}
	
	public Book(String title, String author, String description) {
		this.title = title;
		this.author = author;
		this.description = description;
	}
	
	public Book(int id, String title, String author, String description) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.description = description;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}
	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	private static String formatField(String s) {
		return "\"" + s + "\"";
	}
	
	private static String parseField(String s) {
		if(s.startsWith("\"")) {
			s = s.substring(1);
		}
		if(s.endsWith("\"")) {
			s = s.substring(0, s.length() -1);
		}
		return s;
	}
	
	public static Book parseBook(String s) throws Exception {
		String[] tokens = s.split("\",\"");
		if (tokens.length == 4) {
			return new Book(Integer.parseInt(parseField(tokens[0])), parseField(tokens[1]), parseField(tokens[2]),
					parseField(tokens[3]));
		} else {
			throw new Exception("Invalid Data format");
		}
	}
	
	public String toString() {
		return formatField("" + id) + "," + formatField(title) + "," + formatField(author) + "," + formatField(description);
	}
}
