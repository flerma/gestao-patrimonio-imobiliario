package com.techup.gestao_patrimonio_imobiliario.data.notificacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DispositivoPushRepository extends JpaRepository<DispositivoPushEntity, UUID> {

    Optional<DispositivoPushEntity> findByExpoPushToken(String expoPushToken);

    List<DispositivoPushEntity> findByAtivoTrue();

    boolean existsByExpoPushToken(String expoPushToken);
}
