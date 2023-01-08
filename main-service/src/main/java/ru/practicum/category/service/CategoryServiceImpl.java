package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.storage.CategoryRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.util.PageableRequest;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        List<Category> categories = categoryRepository.findAll(getPageable(from, size)).getContent();
        log.info("CategoryServiceImpl: getAll.");

        return categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(long categoryId) {
        Category category = getByIdAndThrow(categoryId);
        log.info("CategoryServiceImpl: getById. CategoryId {}.", categoryId);

        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto) {
        Category category = getByIdAndThrow(categoryDto.getId());
        category.setName(categoryDto.getName());
        log.info("CategoryServiceImpl: update.");

        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.save(CategoryMapper.fromNewCategoryDto(newCategoryDto));
        log.info("CategoryServiceImpl: create.");

        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public void delete(long categoryId) {
        getById(categoryId);
        categoryRepository.deleteById(categoryId);
        log.info("CategoryServiceImpl: delete. CategoryId {}.", categoryId);
    }

    private Category getByIdAndThrow(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Category {} doesn't exist.", categoryId));
    }

    private Pageable getPageable(Integer from, Integer size) {
        return new PageableRequest(from, size, Sort.unsorted());
    }
}
