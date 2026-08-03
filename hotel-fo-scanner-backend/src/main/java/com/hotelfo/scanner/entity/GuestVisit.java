package com.hotelfo.scanner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Catatan setiap kunjungan/check-in seorang tamu. Satu Guest bisa punya banyak GuestVisit,
 * sehingga tamu yang menginap berulang tidak perlu didata ulang identitasnya dari nol.
 */
@Entity
@Table(name = "guest_visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false, foreignKey = @ForeignKey(name = "fk_visits_guest"))
    private Guest guest;

    @Column(name = "room_number", length = 10)
    private String roomNumber;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "purpose_of_stay", length = 100)
    private String purposeOfStay;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, foreignKey = @ForeignKey(name = "fk_visits_user"))
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
