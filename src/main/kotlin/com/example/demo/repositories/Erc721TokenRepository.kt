package com.example.demo.repositories

import com.example.demo.entities.Erc721TokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository interface for ERC-721 Transfer Token data stored in the database.
 *
 * This interface extends JpaRepository, providing CRUD operations and the ability
 * to execute custom queries on the ERC721TokenEntity table.
 */
@Repository
interface Erc721TokenRepository : JpaRepository<Erc721TokenEntity, Long> {

    /**
     * Returns a distinct set of existing contract addresses if specified contract addresses exists in the database.
     *
     * @param contractAddresses The contract addresses to check.
     * @return Unique contract addresses that matched the specified contract addresses.
     */
    @Query(value = "SELECT DISTINCT t.contractAddress FROM Erc721TokenEntity t WHERE t.contractAddress IN :contractAddresses")
    fun findDistinctContractAddresses(@Param("contractAddresses") contractAddresses: Collection<String>): Set<String>
}