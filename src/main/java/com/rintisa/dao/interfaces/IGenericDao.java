/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.rintisa.dao.interfaces;

import com.rintisa.exception.DatabaseException;

import java.util.List;
import java.util.Optional;

public interface IGenericDao<T, ID> {
    T save(T entity) throws DatabaseException;
    Optional<T> findById(ID id) throws DatabaseException;
    List<T> findAll() throws DatabaseException;
    void delete(ID id) throws DatabaseException;
    void update(T entity) throws DatabaseException;
}
