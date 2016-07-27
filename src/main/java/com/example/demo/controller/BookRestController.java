/**
 * 
 */
package com.example.demo.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.BooksApplication;
import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Robert Tu - Jul 20, 2016
 *
 */
@RestController
public class BookRestController {
	
	@Autowired
	BookRepository bookRepository;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	//-------------------Retrieve All Books--------------------------------------------------------
	
	@RequestMapping(value="/bookmgr", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Book> getBooks(Model model) {
		return getAllBooks();
	}
	
	//-------------------View/Edit a Book--------------------------------------------------------
	
	@RequestMapping(value = "/bookmgr/edit/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Book> getBook(@PathVariable("id") int id) {
        System.out.println("Fetching Book with id " + id);
        Book book = bookRepository.findOne(id);
        if (book == null) {
            System.out.println("Book with id " + id + " not found");
            return new ResponseEntity<Book>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Book>(book, HttpStatus.OK);
    }

	//-------------------Create a Book--------------------------------------------------------
	
	@RequestMapping(value="/bookmgr/new", method=RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Book> createNew(@RequestBody String book, UriComponentsBuilder ucBuilder) {
		String json = parseJsonStr(book);
		//System.out.println("Fetching Book to Create");
		ObjectMapper objectMapper = new ObjectMapper();
		Book savedBook = null;
		try {
			Book myBook = objectMapper.readValue(json.getBytes(), Book.class);
			Book newBook = new Book(myBook.getTitle(), myBook.getAuthor(), myBook.getDescription(), myBook.getIsbn());
			savedBook = bookRepository.save(newBook);
			persistToDisk();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return new ResponseEntity<Book>(savedBook, HttpStatus.CREATED);
	}
	
	//-------------------Update a Book --------------------------------------------------------
	
	@RequestMapping(value="/bookmgr/update/{id}", method=RequestMethod.POST)
	public ResponseEntity<Book> update(@PathVariable("id") int id, @RequestBody String book) {
		String json = parseJsonStr(book);
		//System.out.println("Fetching Book to save with id " + id);
		Book currentBook = bookRepository.findOne(id);
		if (currentBook == null) {
            System.out.println("Book with id " + id + " not found");
            return new ResponseEntity<Book>(HttpStatus.NOT_FOUND);
        }
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Book myBook = objectMapper.readValue(json.getBytes(), Book.class);
			currentBook = bookRepository.save(myBook);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return new ResponseEntity<Book>(currentBook, HttpStatus.OK);
	}
	
	@RequestMapping(value="/bookmgr/search/{searchStr}", method=RequestMethod.GET)
	public List<Book> searchBook(@PathVariable String searchStr) {
		List<Book> books = new ArrayList<>();
		if (searchStr.length() > 0) {
			try {
				books = searchByTitle(searchStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return books;
	}
	
	//------------------- Delete a Book --------------------------------------------------------
    
    @RequestMapping(value = "/bookmgr/delete/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Book> removeBook(@PathVariable("id") int id) {
        //System.out.println("Fetching & Deleting User with id " + id);
        Book book = bookRepository.findOne(id);
        if (book == null) {
            System.out.println("Unable to delete. User with id " + id + " not found");
            return new ResponseEntity<Book>(HttpStatus.NOT_FOUND);
        }
        bookRepository.delete(id);
		persistToDisk();
		return new ResponseEntity<Book>(book, HttpStatus.OK);
    }
	
	protected Connection getConnection() throws SQLException {
		return jdbcTemplate.getDataSource().getConnection();
	}
	
	protected List<Book> getAllBooks() {
		return (List<Book>) bookRepository.findAll();
	}
	
	protected void persistToDisk() {
		try {
			BooksApplication.saveBooksToDisk((List<Book>) bookRepository.findAll());
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * 
	 * @param title
	 * @return
	 * @throws Exception
	 */
	protected List<Book> searchByTitle(String searchStr) throws Exception {
		Pattern pattern = Pattern.compile(".*\\D.*");
		boolean isNumber = !pattern.matcher(searchStr).matches();
		List<Object> params = new ArrayList<>();
		String sql = "";
		if(isNumber) {
			params.add(new Integer(searchStr));
			sql = "Select * from books where id = ?";
		} else {
			for(int i=0; i<3; i++) {
				params.add(searchStr);
			}
			sql = "Select * from books where lower(title) like ? or lower(author) like ? or lower(description) like ?";
		}
		List<Book> books = new ArrayList<>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);
			for(int i=0; i<params.size(); i++) {
				if(isNumber) {
					pstmt.setObject(i+1, params.get(i));
				} else {
					pstmt.setObject(i+1, "%" + params.get(i) + "%");
				}
			}
			rs = pstmt.executeQuery();
			while(rs.next()) {
				books.add(new Book(rs.getInt("id"), 
						rs.getString("title"), 
						rs.getString("author"), 
						rs.getString("description"),
						rs.getString("isbn")));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if(rs != null) rs.close();
			if(pstmt != null) pstmt.close();
			if(con != null) con.close();
		}
		return books;
	}
	
	private static String parseJsonStr(String json) {
		JSONObject jsonObject = new JSONObject(json);
		Iterator<String> it = jsonObject.keys();
		if(it.hasNext()) {
			return jsonObject.getJSONObject(it.next()).toString();
		}
		return json;
	}
}
