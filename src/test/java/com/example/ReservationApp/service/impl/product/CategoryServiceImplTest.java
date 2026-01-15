package com.example.ReservationApp.service.impl.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.product.CategoryDTO;
import com.example.ReservationApp.entity.product.Category;
import com.example.ReservationApp.enums.CategoryStatus;
import com.example.ReservationApp.mapper.CategoryMapper;
import com.example.ReservationApp.repository.product.CategoryRepository;

class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        category = new Category();
        category.setId(1L);
        category.setName("Food");
        category.setDescription("Food category");
        category.setStatus(CategoryStatus.ACTIVE);

        categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setName("Food");
        categoryDTO.setDescription("Food category");
        categoryDTO.setStatus(CategoryStatus.ACTIVE);
    }

    @Test
    void testCreateCategorySuccess() {
        when(categoryRepository.existsByname(categoryDTO.getName())).thenReturn(false);
        when(categoryMapper.toEntity(any(CategoryDTO.class))).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        ResponseDTO<CategoryDTO> response = categoryService.createCategory(categoryDTO);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("新しいカテゴリの追加に成功しました", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void testGetAllCategories() {
        when(categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id")))
                .thenReturn(List.of(category));
        when(categoryMapper.toDTO(any(Category.class)))
                .thenReturn(categoryDTO);

        ResponseDTO<List<CategoryDTO>> response = categoryService.getAllCategories();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(1, response.getData().size());
        assertEquals("Food", response.getData().get(0).getName());
    }

    @Test
    void testGetCategoryByIdSuccess() {
        when(categoryRepository.findById(anyLong()))
                .thenReturn(Optional.of(category));
        when(categoryMapper.toDTO(any(Category.class)))
                .thenReturn(categoryDTO);

        ResponseDTO<CategoryDTO> response = categoryService.getCategoryById(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Food", response.getData().getName());
    }

    @Test
    void testUpdateCategorySuccess() {
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setName("Updated Food");

        when(categoryRepository.findById(anyLong()))
                .thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class)))
                .thenReturn(category);
        when(categoryMapper.toDTO(any(Category.class)))
                .thenReturn(categoryDTO);

        ResponseDTO<CategoryDTO> response = categoryService.updateCategory(1L, updateDTO);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("編集に成功しました", response.getMessage());
    }

    @Test
    void testDeleteCategorySuccess() {
        when(categoryRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(anyLong());
        doNothing().when(categoryRepository).flush();

        ResponseDTO<Void> response = categoryService.deleteCategory(1L);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("削除に成功しました", response.getMessage());
    }
}
