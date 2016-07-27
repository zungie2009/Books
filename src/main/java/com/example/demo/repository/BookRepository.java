/**
 * 
 */
package com.example.demo.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.model.Book;

/**
 * @author Robert Tu - Jul 17, 2016
 *
 */
public interface BookRepository extends CrudRepository<Book, Integer> {

}
