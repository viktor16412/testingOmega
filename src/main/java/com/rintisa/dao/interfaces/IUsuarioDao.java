package com.rintisa.dao.interfaces;

import com.rintisa.model.Usuario;
import com.rintisa.exception.DatabaseException;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;


public interface IUsuarioDao extends IGenericDao<Usuario, Long> {
    Optional<Usuario> findByUsername(String username) throws DatabaseException;
    List<Usuario> findByActivo(boolean activo) throws DatabaseException;
    boolean existsByUsername(String username) throws DatabaseException;
    boolean existsByEmail(String email) throws DatabaseException;
    void actualizarUltimoAcceso(Long userId, LocalDateTime fecha) throws DatabaseException;
    Optional<LocalDateTime> obtenerUltimoAcceso(Long userId) throws DatabaseException; 
}