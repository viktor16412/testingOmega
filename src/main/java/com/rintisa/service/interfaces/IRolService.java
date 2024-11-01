/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.rintisa.service.interfaces;

import com.rintisa.model.Rol;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import java.util.List;
import java.util.Optional;

public interface IRolService {
    Rol crear(Rol rol) throws DatabaseException, ValidationException;
    
    void actualizar(Rol rol) throws DatabaseException, ValidationException;
    
    void eliminar(Long id) throws DatabaseException;
    
    Optional<Rol> buscarPorId(Long id) throws DatabaseException;
    
    Optional<Rol> buscarPorNombre(String nombre) throws DatabaseException;
    
    List<Rol> listarTodos() throws DatabaseException;
    
    List<Rol> buscarPorActivo(boolean activo) throws DatabaseException;
    
    List<Rol> buscar(String criterio) throws DatabaseException;
}