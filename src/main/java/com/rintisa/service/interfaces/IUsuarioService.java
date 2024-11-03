package com.rintisa.service.interfaces;

import com.rintisa.model.Usuario;
import com.rintisa.model.RegistroAcceso;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface IUsuarioService {
    Usuario crear(Usuario usuario) throws DatabaseException, ValidationException;
    
    void actualizar(Usuario usuario) throws DatabaseException, ValidationException;
    
    void eliminar(Long id) throws DatabaseException;
    
    Optional<Usuario> buscarPorId(Long id) throws DatabaseException;
    
    Optional<Usuario> buscarPorUsername(String username) throws DatabaseException;
    
    List<Usuario> listarTodos() throws DatabaseException;
    
    List<Usuario> buscarPorActivo(boolean activo) throws DatabaseException;
    
    boolean autenticar(String username, String password) throws DatabaseException;
    
    void cambiarPassword(Long userId, String oldPassword, String newPassword) 
        throws DatabaseException, ValidationException;
    
    void actualizarUltimoAcceso(Long userId) throws DatabaseException;
    Optional<LocalDateTime> obtenerUltimoAcceso(Long userId) throws DatabaseException;
    void registrarAcceso(Usuario usuario, 
                        RegistroAcceso.TipoAcceso tipoAcceso, 
                        String ipAddress, 
                        String detalles) throws DatabaseException;
    List<RegistroAcceso> obtenerRegistroAccesos() throws DatabaseException;
}
