package com.example.demo.daos

import com.example.demo.dtos.Erc721TokenDto
import com.example.demo.entities.Erc721TokenEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

/**
 * Data Access Object (DAO) class for managing complex queries of ERC-721 Transfer Token event queries.
 *
 * @property entityManager EntityManager to facilitate database operations.
 */
@Repository
class Erc721TokenDao(
    @PersistenceContext
    private val entityManager: EntityManager
) {

    /**
     * Retrieves a paginated list of ERC-721 Token Dto objects based on filter criteria.
     *
     * This method constructs a criteria query using the provided DTO to filter results.
     * Pagination is applied according to the Pageable argument, and results
     * are mapped from ERC721TokenEntity to ERC721TokenDto.
     *
     * @param dto The ERC-721 Token Dto object containing filter criteria.
     * @param pageable The pagination configuration used to limit and paginate the result set.
     * @return A paginated page of ERC-721 Token Dto representing the filtered and mapped results.
     */
    @Transactional(readOnly = true)
    fun findAllTokensByFilter(dto: Erc721TokenDto, pageable: Pageable): Page<Erc721TokenEntity> {
        val cb = entityManager.criteriaBuilder
        val cq = cb.createQuery(Erc721TokenEntity::class.java)
        val root = cq.from(Erc721TokenEntity::class.java)
        val predicates = mutableListOf<Predicate>()

        // If field value from dto is specified, add it to predicates list.
        predicates.addValuesIfNotNull(dto, cb, root)

        // If predicates are specified, apply as filter. Otherwise, return everything.
        if (predicates.isNotEmpty()) {
            cq.where(cb.and(*predicates.toTypedArray()))
        }

        val query = entityManager.createQuery(cq.select(root))
        query.firstResult = pageable.offset.toInt()
        query.maxResults = pageable.pageSize
        val result = query.resultList
        val totalRows = result.size
        return PageImpl(result, pageable, totalRows.toLong())
    }

    /**
     * Adds predicates to the provided mutable list.
     *
     * Iterates through all properties of the provided ERC-721 Token Dto, checks if they are non-null, and adds appropriate
     * predicates to the list. Supports specific types: BigInteger for exact match and String for case-insensitive match.
     *
     * @param dto The ERC-721 Token Dto object with filter criteria.
     * @param cb The CriteriaBuilder used to create the query predicates.
     * @param root The root entity model from which the query predicates are derived.
     * @throws Exception If an unsupported data type is encountered in the Dto's properties.
     */
    private fun MutableList<Predicate>.addValuesIfNotNull(
        dto: Erc721TokenDto,
        cb: CriteriaBuilder,
        root: Root<Erc721TokenEntity>
    ) {
        Erc721TokenDto::class.memberProperties.forEach { prop ->
            val value = prop.get(dto)
            if (value != null) {
                when (val type = prop.returnType.javaType) {
                    BigInteger::class.java -> {
                        this.add(cb.equal(root.get<BigInteger>(prop.name), value))
                    }

                    String::class.java -> {
                        this.add(cb.equal(cb.lower(root.get(prop.name)), value.toString().lowercase()))
                    }

                    else -> throw Exception(
                        "Only String and BigInteger are used in ERC721TokenDto's fields " +
                                "(${type} used for ${prop.name} field)."
                    )
                }
            }
        }
    }
}