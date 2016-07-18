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
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.BooksApplication;
import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

/**
 * @author Robert Tu
 *
 */
@Controller
public class BookController {
	private String searchStr = "";
	
	@Autowired
	protected BookRepository bookRepository;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@RequestMapping(value="/books", method=RequestMethod.GET)
	public String getBooks(Model model) {
		model.addAttribute("books", getAllBooks());
		model.addAttribute("searchStr", searchStr);
		return "list";
	}
	
	@RequestMapping(value="/books/Edit/{id}", method=RequestMethod.GET)
	public String edit(@PathVariable Integer id, Model model) {
		Book book = bookRepository.findOne(id);
		model.addAttribute("book", book);
		model.addAttribute("searchStr", searchStr);
		return "edit";
	}
	
	@RequestMapping(value="/books/Delete/{id}", method=RequestMethod.GET)
	public ModelAndView delete(@PathVariable Integer id, Model model) {
		bookRepository.delete(id);
		persistToDisk();
		model.addAttribute("searchStr", searchStr);
		return new ModelAndView("redirect:/books");
	}
	
	@RequestMapping(value="/books/New", method=RequestMethod.GET)
	public String newBook(Model model) {
		model.addAttribute("searchStr", searchStr);
		return "new";
	}
	
	@RequestMapping(value="/books/addNew", method=RequestMethod.POST)
	public ModelAndView addNew(@RequestParam("title") String title, 
			@RequestParam("author") String author, 
			@RequestParam("description") String description) {
		bookRepository.save(new Book(title, author, description));
		persistToDisk();
		return new ModelAndView("redirect:/books");
	}
	
	@RequestMapping(value="/books/update", method=RequestMethod.POST)
	public ModelAndView update(@RequestParam("id") Integer id, 
			@RequestParam("title") String title, 
			@RequestParam("author") String author, 
			@RequestParam("description") String description) {
		Book book = bookRepository.findOne(id);
		book.setTitle(title);
		book.setAuthor(author);
		book.setDescription(description);
		bookRepository.save(book);
		persistToDisk();
		return new ModelAndView("redirect:/books");
	}
	
	@RequestMapping(value="/books/Search", method=RequestMethod.POST)
	public String searchBook(@RequestParam("titleText") String titleText, Model model) {
		List<Book> books = new ArrayList<>();
		try {
			books = searchByTitle(titleText);
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("books", books);
		model.addAttribute("searchStr", titleText);
		return "list";
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
						rs.getString("description")));
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
}
