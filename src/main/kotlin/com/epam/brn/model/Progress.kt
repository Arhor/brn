package com.epam.brn.model

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
data class Progress(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    @Column(nullable = false)
    val progress: String
) {
    @OneToOne(cascade = [(CascadeType.ALL)])
    val exerciseGroup: ExerciseGroup? = null
}