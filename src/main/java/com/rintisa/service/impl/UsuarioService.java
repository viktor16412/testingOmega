package com.rintisa.service.impl;

import com.rintisa.config.DatabaseConfig;
import com.rintisa.dao.interfaces.IUsuarioDao;
import com.rintisa.service.interfaces.IUsuarioService;
import com.rintisa.model.Usuario;
import com.rintisa.exception.DatabaseException;
import com.rintisa.exception.ValidationException;
import com.rintisa.model.RegistroAcceso;
import com.rintisa.util.ValidationUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioService implements IUsuarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private final IUsuarioDao usuarioDao;
    
    public UsuarioService(IUsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }
    
    @Override
    public Usuario crear(Usuario usuario) throws DatabaseException, ValidationException {
        logger.debug("Creando nuevo usuario: {}", usuario.getUsername());
        
        // Validar datos del usuario
        ValidationUtils.validarUsuario(
            usuario.getUsername(), 
            usuario.getPassword(), 
            usuario.getEmail()
        );
        
        // Verificar si ya existe el username o email
        if (usuarioDao.existsByUsername(usuario.getUsername())) {
            throw new ValidationException("username", "El nombre de usuario ya está en uso");
        }
        
        if (usuarioDao.existsByEmail(usuario.getEmail())) {
            throw new ValidationException("email", "El email ya está registrado");
        }
        
        // Encriptar contraseña
        String hashedPassword = BCrypt.hashpw(usuario.getPassword(), BCrypt.gensalt());
        usuario.setPassword(hashedPassword);
        
        // Establecer fecha de creación
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setActivo(true);
        
        try {
            Usuario usuarioCreado = usuarioDao.save(usuario);
            logger.info("Usuario creado exitosamente: {}", usuario.getUsername());
            return usuarioCreado;
        } catch (DatabaseException e) {
            logger.error("Error al crear usuario: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void actualizar(Usuario usuario) throws DatabaseException, ValidationException {
        logger.debug("Actualizando usuario: {}", usuario.getUsername());
        
        // Verificar que el usuario existe
        Optional<Usuario> usuarioExistente = usuarioDao.findById(usuario.getId());
        if (!usuarioExistente.isPresent()) {
            throw new ValidationException("id", "El usuario no existe");
        }
        
        // Validar email si ha cambiado
        if (!usuarioExistente.get().getEmail().equals(usuario.getEmail())) {
            ValidationUtils.validarEmail(usuario.getEmail());
            if (usuarioDao.existsByEmail(usuario.getEmail())) {
                throw new ValidationException("email", "El email ya está registrado");
            }
        }
        
        try {
            usuarioDao.update(usuario);
            logger.info("Usuario actualizado exitosamente: {}", usuario.getUsername());
        } catch (DatabaseException e) {
            logger.error("Error al actualizar usuario: {}", e.getMessage());
            throw e;
        }
    }
    
    @Override
    public Optional<Usuario> buscarPorId(Long id) throws DatabaseException {
        logger.debug("Buscando usuario por ID: {}", id);
        try {
            return usuarioDao.findById(id);
        } catch (DatabaseException e) {
            logger.error("Error al buscar usuario por ID: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) throws DatabaseException {
        logger.debug("Buscando usuario por username: {}", username);
        try {
            return usuarioDao.findByUsername(username);
        } catch (DatabaseException e) {
            logger.error("Error al buscar usuario por username: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Usuario> listarTodos() throws DatabaseException {
       
        try {
            logger.debug("Listando todos los usuarios");
            return usuarioDao.findAll();
        } catch (DatabaseException e) {
            logger.error("Error al listar usuarios: {}", e.getMessage());
            return new ArrayList<>(); // Retorna lista vacía en caso de error
        }
    }

    @Override
    public List<Usuario> buscarPorActivo(boolean activo) throws DatabaseException {
        logger.debug("Buscando usuarios por estado activo: {}", activo);
        try {
            return usuarioDao.findByActivo(activo);
        } catch (DatabaseException e) {
            logger.error("Error al buscar usuarios por estado activo: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean autenticar(String username, String password) throws DatabaseException {
        logger.debug("Intentando autenticar usuario: {}", username);
        
        try {
            Optional<Usuario> usuarioOpt = usuarioDao.findByUsername(username);
            
            if (!usuarioOpt.isPresent()) {
                logger.warn("Usuario no encontrado: {}", username);
                return false;
            }
            
            Usuario usuario = usuarioOpt.get();
            
            if (!usuario.isActivo()) {
                logger.warn("Intento de login con usuario inactivo: {}", username);
                return false;
            }
            
            // Verificar contraseña con BCrypt
            boolean autenticado = BCrypt.checkpw(password, usuario.getPassword());
            
            if (autenticado) {
                logger.info("Usuario autenticado exitosamente: {}", username);
                actualizarUltimoAcceso(usuario.getId());
            } else {
                logger.warn("Contraseña incorrecta para usuario: {}", username);
            }
            
            return autenticado;
            
        } catch (DatabaseException e) {
            logger.error("Error durante la autenticación de usuario: {}", username, e);
            throw e;
        }
    }
    
    
    @Override
    public void eliminar(Long id) throws DatabaseException {
        logger.debug("Eliminando usuario con ID: {}", id);
        
        try {
            Optional<Usuario> usuario = usuarioDao.findById(id);
            if (!usuario.isPresent()) {
                logger.warn("Intento de eliminar usuario inexistente: {}", id);
               // throw new ValidationException("id", "El usuario no existe");//revisar
            }
            
            usuarioDao.delete(id);
            logger.info("Usuario eliminado exitosamente: {}", id);
            
        } catch (DatabaseException e) {
            logger.error("Error al eliminar usuario: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void cambiarPassword(Long userId, String oldPassword, String newPassword) 
            throws DatabaseException, ValidationException {
        logger.debug("Cambiando contraseña para usuario ID: {}", userId);
        
        // Validar nueva contraseña
        ValidationUtils.validarPassword(newPassword);
        
        try {
            Optional<Usuario> usuarioOpt = usuarioDao.findById(userId);
            if (!usuarioOpt.isPresent()) {
                throw new ValidationException("id", "El usuario no existe");
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar contraseña actual
            if (!BCrypt.checkpw(oldPassword, usuario.getPassword())) {
                throw new ValidationException("oldPassword", "La contraseña actual es incorrecta");
            }
            
            // Encriptar y guardar nueva contraseña
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            usuario.setPassword(hashedPassword);
            
            usuarioDao.update(usuario);
            logger.info("Contraseña cambiada exitosamente para usuario: {}", usuario.getUsername());
            
        } catch (DatabaseException e) {
            logger.error("Error al cambiar contraseña: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void actualizarUltimoAcceso(Long userId) throws DatabaseException {
        try {
            // Verificar que el usuario existe
            Optional<Usuario> usuario = usuarioDao.findById(userId);
            if (!usuario.isPresent()) {
                logger.warn("Intento de actualizar último acceso de usuario inexistente: {}", userId);
                throw new DatabaseException("Usuario no encontrado");
            }
            
            // Actualizar último acceso
            LocalDateTime ahora = LocalDateTime.now();
            usuarioDao.actualizarUltimoAcceso(userId, ahora);
            
            // Actualizar el objeto usuario si es el usuario actual
            usuario.get().setUltimoAcceso(ahora);
            
            logger.debug("Último acceso actualizado para usuario: {}", usuario.get().getUsername());
            
        } catch (DatabaseException e) {
            logger.error("Error al actualizar último acceso: {}", e.getMessage());
            throw e;
        }
    }
    
    @Override
    public Optional<LocalDateTime> obtenerUltimoAcceso(Long userId) throws DatabaseException {
        try {
            return usuarioDao.obtenerUltimoAcceso(userId);
        } catch (DatabaseException e) {
            logger.error("Error al obtener último acceso: {}", e.getMessage());
            throw e;
        }
    }
    
   @Override
     public List<RegistroAcceso> obtenerRegistroAccesos() throws DatabaseException {
        try {
            String sql = "SELECT a.id, a.usuario_id, a.fecha_acceso, a.tipo_acceso, " +
                        "a.ip_address, a.detalles, u.username, u.nombre, u.apellido " +
                        "FROM accesos a " +
                        "INNER JOIN usuarios u ON a.usuario_id = u.id " +
                        "ORDER BY a.fecha_acceso DESC";
            
            List<RegistroAcceso> registros = new ArrayList<>();
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    RegistroAcceso registro = new RegistroAcceso();
                    registro.setId(rs.getLong("id"));
                    registro.setFechaAcceso(rs.getTimestamp("fecha_acceso").toLocalDateTime());
                    registro.setTipoAcceso(RegistroAcceso.TipoAcceso.valueOf(rs.getString("tipo_acceso")));
                    registro.setIpAddress(rs.getString("ip_address"));
                    registro.setDetalles(rs.getString("detalles"));
                    
                    // Crear usuario con datos básicos
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getLong("usuario_id"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setNombre(rs.getString("nombre"));
                    usuario.setApellido(rs.getString("apellido"));
                    
                    registro.setUsuario(usuario);
                    registros.add(registro);
                }
            }
            
            return registros;
            
        } catch (SQLException e) {
            logger.error("Error al obtener registro de accesos", e);
            throw new DatabaseException("Error al obtener registro de accesos: " + e.getMessage());
        }
    }

    // Método para registrar un nuevo acceso
     @Override
    public void registrarAcceso(Usuario usuario, 
                              RegistroAcceso.TipoAcceso tipoAcceso, 
                              String ipAddress, 
                              String detalles) throws DatabaseException {
        try {
            String sql = "INSERT INTO accesos (usuario_id, tipo_acceso, ip_address, detalles) " +
                        "VALUES (?, ?, ?, ?)";
            
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setLong(1, usuario.getId());
                stmt.setString(2, tipoAcceso.name());
                stmt.setString(3, ipAddress);
                stmt.setString(4, detalles);
                
                stmt.executeUpdate();
                
                logger.info("Acceso registrado - Usuario: {}, Tipo: {}", 
                          usuario.getUsername(), tipoAcceso);
            }
        } catch (SQLException e) {
            logger.error("Error al registrar acceso", e);
            throw new DatabaseException("Error al registrar acceso: " + e.getMessage());
        }
    }
    
    

    
    
    
    // Métodos de utilidad privados
    
    private void validarUsuarioExistente(Long id) throws ValidationException, DatabaseException {
        if (!usuarioDao.findById(id).isPresent()) {
            throw new ValidationException("id", "El usuario no existe");
        }
    }
    
    private void validarUsername(String username, Long excludeId) throws ValidationException, DatabaseException {
        Optional<Usuario> existente = usuarioDao.findByUsername(username);
        if (existente.isPresent() && !existente.get().getId().equals(excludeId)) {
            throw new ValidationException("username", "El nombre de usuario ya está en uso");
        }
    }
    
    private void validarEmail(String email, Long excludeId) throws ValidationException, DatabaseException {
        Optional<Usuario> existente = usuarioDao.findByUsername(email);
        if (existente.isPresent() && !existente.get().getId().equals(excludeId)) {
            throw new ValidationException("email", "El email ya está registrado");
        }
    }
    
    private boolean verificarPassword(String passwordIngresada, String passwordAlmacenada) {
        // Aquí deberías usar BCrypt o similar para verificar la contraseña
        return BCrypt.checkpw(passwordIngresada, passwordAlmacenada);
    }
     public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
    
}
