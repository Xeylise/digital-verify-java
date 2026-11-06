package com.digitalverify.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa una apuesta deportiva realizada por un jugador en la plataforma.
 * Permite visualizar el historial de transacciones y cuotas en la seccion "My Bets".
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
public class Bet {

    /**
     * Identificador unico de la apuesta.
     */
    private Long id;

    /**
     * Codigo de referencia unico de la apuesta (ej: BET-8921).
     */
    private String betReference;

    /**
     * Identificador del usuario jugador propietario de la apuesta.
     */
    private Long userId;

    /**
     * Nombre del evento deportivo o partida Esports.
     */
    private String eventName;

    /**
     * Pronostico o seleccion realizada por el usuario.
     */
    private String selection;

    /**
     * Cuota multiplicadora asignada a la seleccion.
     */
    private BigDecimal odds;

    /**
     * Monto apostado por el jugador.
     */
    private BigDecimal stake;

    /**
     * Ganancia potencial o monto pagado al jugador.
     */
    private BigDecimal payout;

    /**
     * Estado de la apuesta (WON, LOST, PENDING).
     */
    private String status;

    /**
     * Fecha y hora en que se coloco la apuesta.
     */
    private LocalDateTime placedAt;

    /**
     * Constructor vacio para mapeo en base de datos.
     */
    public Bet() {}

    /**
     * Constructor con todos los atributos de la apuesta.
     *
     * @param id           Identificador de la apuesta.
     * @param betReference Codigo de referencia.
     * @param userId       ID del jugador.
     * @param eventName    Nombre del evento.
     * @param selection    Seleccion/pronostico.
     * @param odds         Cuota.
     * @param stake        Importe apostado.
     * @param payout       Pago potencial.
     * @param status       Estado de la apuesta.
     * @param placedAt     Fecha y hora de colocacion.
     */
    public Bet(Long id, String betReference, Long userId, String eventName, String selection,
               BigDecimal odds, BigDecimal stake, BigDecimal payout, String status, LocalDateTime placedAt) {
        this.id = id;
        this.betReference = betReference;
        this.userId = userId;
        this.eventName = eventName;
        this.selection = selection;
        this.odds = odds;
        this.stake = stake;
        this.payout = payout;
        this.status = status;
        this.placedAt = placedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBetReference() { return betReference; }
    public void setBetReference(String betReference) { this.betReference = betReference; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getSelection() { return selection; }
    public void setSelection(String selection) { this.selection = selection; }

    public BigDecimal getOdds() { return odds; }
    public void setOdds(BigDecimal odds) { this.odds = odds; }

    public BigDecimal getStake() { return stake; }
    public void setStake(BigDecimal stake) { this.stake = stake; }

    public BigDecimal getPayout() { return payout; }
    public void setPayout(BigDecimal payout) { this.payout = payout; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }
}
