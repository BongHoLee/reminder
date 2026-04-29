package com.bong.reminder.list.adapter.out.persistence

import com.bong.reminder.list.application.port.out.ReminderListRepositoryPort
import com.bong.reminder.list.domain.ReminderList
import org.springframework.stereotype.Component

@Component
class ReminderListPersistenceAdapter(
    private val jpaRepository: ReminderListJpaRepository,
) : ReminderListRepositoryPort {

    override fun save(list: ReminderList): ReminderList = jpaRepository.save(list)

    override fun findById(id: Long): ReminderList? = jpaRepository.findById(id).orElse(null)

    override fun findAllOrdered(): List<ReminderList> = jpaRepository.findAllByOrderBySortOrderAsc()

    override fun existsById(id: Long): Boolean = jpaRepository.existsById(id)

    override fun deleteById(id: Long) {
        jpaRepository.deleteById(id)
    }
}
