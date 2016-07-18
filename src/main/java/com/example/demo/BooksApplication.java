package com.example.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

@SpringBootApplication
public class BooksApplication implements CommandLineRunner {
	public static final String BOOK_FILE = "books.csv"; // disk file to store book data
	
	@Autowired
	protected BookRepository bookRepository;
	
	public static void main(String[] args) {
		SpringApplication.run(BooksApplication.class, args);
	}

	@Override
	public void run(String... arg0) throws Exception {
		File file = new File(BOOK_FILE);
		if (file.exists() && file.isFile()) {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = in.readLine()) != null) {
				try {
					Book book = Book.parseBook(line);
					bookRepository.save(book);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Failed Parsing line: " + line);
				}
			}
			in.close();
		} else {
			List<Book> books = getBooks();
			// Use Lambda expression to store books
			books.stream().forEach(book -> bookRepository.save(book));
			List<Book> savedBooks = (List<Book>) bookRepository.findAll();
			saveBooksToDisk(savedBooks);
		}
	}
	
	public static void saveBooksToDisk(List<Book> books) throws IOException {
		File file = new File(BOOK_FILE);
		if(!file.exists()) {
			file.createNewFile();
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		for(Book book : books) {
			out.write(book.toString() + "\r\n");
		}
		out.flush();
		out.close();
	}
	
	public static List<Book> getBooks() {
		List<Book> books = new ArrayList<>();
		books.add(new Book("The Hobbit", "J. R. R. Tolkien", "A Children Fantasy Book"));
		books.add(new Book("Lord of the Rings", "J. R. R. Tolkien", "A Children Fantasy Book"));
		books.add(new Book("Snow White and the Seven Dwarfs", "Brothers Grimm", "A Children Fantasy Book"));
		books.add(new Book("Moby Dick", "Herman Melville", "About the big Whale"));
		books.add(new Book("Snow Crash", "Neal Stephenson", "A Science Fiction Story"));
		//books.add(new Book("Game of Thrones", "George R. R. Martin", "A Fantasy Fiction Story"));
		return books;
	}
	
}
