/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.rintisa.dao.interfaces;

import com.rintisa.model.Usuario;
import com.rintisa.exception.DatabaseException;

import java.util.List;
import java.util.Optional;

public interface IUsuarioDao extends IGenericDao<Usuario, Long> {
    Optional<Usuario> findByUsername(String username) throws DatabaseException;
    List<Usuario> findByActivo(boolean activo) throws DatabaseException;
    boolean existsByUsername(String username) throws DatabaseException;
    boolean existsByEmail(String email) throws DatabaseException;
}