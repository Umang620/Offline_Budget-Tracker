package com.example.magtipidka.data.repository

import com.example.magtipidka.data.local.dao.CategoryDao
import com.example.magtipidka.data.local.database.DefaultCategories
import com.example.magtipidka.data.mapper.toDomain
import com.example.magtipidka.data.mapper.toEntity
import com.example.magtipidka.domain.model.Category
import com.example.magtipidka.domain.model.CategoryType
import com.example.magtipidka.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list ->
            if (list.isEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    insertDefaultCategoriesIfNeeded()
                }
                DefaultCategories.list.mapIndexed { index, entity ->
                    entity.copy(id = (index + 1).toLong()).toDomain()
                }
            } else {
                list.map { it.toDomain() }.distinctBy { Pair(it.name.lowercase().trim(), it.type) }
            }
        }
    }

    override fun getCategoriesByType(type: CategoryType): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(type.name).map { list ->
            if (list.isEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    insertDefaultCategoriesIfNeeded()
                }
                DefaultCategories.list
                    .filter { it.type == type.name || it.type == "ALL" }
                    .mapIndexed { index, entity ->
                        entity.copy(id = (index + 1).toLong()).toDomain()
                    }
            } else {
                list.map { it.toDomain() }.distinctBy { it.name.lowercase().trim() }
            }
        }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        val category = categoryDao.getCategoryById(id)?.toDomain()
        if (category != null) return category

        // Fallback for default categories if DB isn't seeded yet
        val defaultList = DefaultCategories.list.mapIndexed { index, entity ->
            entity.copy(id = (index + 1).toLong()).toDomain()
        }
        return defaultList.find { it.id == id }
    }

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category.toEntity())
    }

    override suspend fun insertDefaultCategoriesIfNeeded() {
        if (categoryDao.getCategoryCount() == 0) {
            categoryDao.insertCategories(DefaultCategories.list)
        }
    }
}
