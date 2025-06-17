package com.example.productservice.service

import com.example.productservice.dto.CategoryDto
import com.example.productservice.model.Category
import com.example.productservice.repository.CategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    fun createCategory(request: CategoryDto): CategoryDto {
        val parentCategory = request.parentCategoryId?.let { parentId ->
            categoryRepository.findById(parentId).orElseThrow {
                RuntimeException("Parent category not found with id: $parentId")
            }
        }

        val category = Category(
            name = request.name,
            description = request.description,
            parentCategory = parentCategory
        )

        val savedCategory = categoryRepository.save(category)
        return savedCategory.toDto()
    }

    fun getCategoryById(id: UUID): CategoryDto? {
        return categoryRepository.findById(id).orElse(null)?.toDto()
    }

    fun getAllCategories(): List<CategoryDto> {
        return categoryRepository.findAll().map { it.toDto() }
    }

    fun updateCategory(id: UUID, request: CategoryDto): CategoryDto {
        val existingCategory = categoryRepository.findById(id).orElseThrow {
            RuntimeException("Category not found with id: $id")
        }

        val parentCategory = request.parentCategoryId?.let { parentId ->
            categoryRepository.findById(parentId).orElseThrow {
                RuntimeException("Parent category not found with id: $parentId")
            }
        }

        val updatedCategory = existingCategory.copy(
            name = request.name,
            description = request.description,
            parentCategory = parentCategory
        )

        val savedCategory = categoryRepository.save(updatedCategory)
        return savedCategory.toDto()
    }

    fun deleteCategory(id: UUID) {
        val category = categoryRepository.findById(id).orElseThrow {
            RuntimeException("Category not found with id: $id")
        }
        categoryRepository.delete(category)
    }

    private fun Category.toDto(): CategoryDto {
        return CategoryDto(
            id = this.id,
            name = this.name,
            description = this.description,
            parentCategoryId = this.parentCategory?.id,
            childCategories = this.childCategories.map { it.toDto() }
        )
    }
}
