package com.rintisa.service.interfaces;

import com.rintisa.exception.ReportException;
import java.time.LocalDateTime;

public interface IRecepcionReporteService {
    // Reportes de Recepciones
    byte[] generarReporteGeneral(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
        throws ReportException;
    
  // Tipos de períodos para reportes
    enum TipoPeriodo {
        DIARIO,
        SEMANAL,
        MENSUAL,
        TRIMESTRAL,
        ANUAL
    }
}