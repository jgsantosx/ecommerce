package com.ecommerce.support;

import com.ecommerce.category.Category;
import com.ecommerce.product.Product;
import com.ecommerce.user.User;
import com.ecommerce.user.UserRole;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fábrica de entidades para testes de unidade.
 *
 * Os ids são gerados pelo banco e não têm setter público, então usamos
 * reflexão (o mesmo recurso já usado no SecurityConfigTest).
 */
public final class TestEntities {

    private TestEntities() {
    }

    public static <T> T withId(T entity, Long id) {
        try {
            Field field = findField(entity.getClass(), "id");
            field.setAccessible(true);
            field.set(entity, id);
            return entity;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static User user(Long id, UserRole role) {
        User user = new User();
        user.setName("Usuário " + id);
        user.setEmail("user" + id + "@email.com");
        user.setPassword("$2a$10$hash");
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return withId(user, id);
    }

    public static Product product(Long id, String name, String price) {
        Category category = new Category();
        category.setName("Categoria");
        category.setCreatedAt(LocalDateTime.now());

        Product product = new Product();
        product.setName(name);
        product.setSku("SKU-" + id);
        product.setPrice(new BigDecimal(price));
        product.setActive(true);
        product.setCategory(withId(category, 1L));
        product.setCreatedAt(LocalDateTime.now());
        return withId(product, id);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // tenta na superclasse
            }
        }
        throw new NoSuchFieldException(name);
    }
}
