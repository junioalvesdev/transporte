package com.ovg.transportes.repository;

import com.ovg.transportes.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Carrega os perfis junto (JOIN FETCH) porque o login precisa deles logo
    // em seguida para montar as permissoes do usuario.
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.perfis WHERE u.loginAd = :loginAd")
    Optional<Usuario> buscarPorLoginAd(@Param("loginAd") String loginAd);
}
