package com.gap.ecommerceapp.service;

import com.gap.ecommerceapp.dto.CartResponse;
import com.gap.ecommerceapp.dto.CartItemResponse;
import com.gap.ecommerceapp.model.Cart;
import com.gap.ecommerceapp.model.CartItem;
import com.gap.ecommerceapp.model.Product;
import com.gap.ecommerceapp.model.User;
import com.gap.ecommerceapp.repository.CartRepository;
import com.gap.ecommerceapp.repository.CartItemRepository;
import com.gap.ecommerceapp.exception.ResourceNotFoundException;
import com.gap.ecommerceapp.exception.InsufficientStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartResponse getCartByUserId(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        List<CartItemResponse> cartItemResponses = cartItems.stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return CartResponse.builder()
                .userId(userId)
                .items(cartItemResponses)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }

    @Transactional
    public CartItemResponse addToCart(User user, Long productId, Integer quantity) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock. Available: " + product.getStockQuantity() + ", Requested: " + quantity);
        }

        // Find or create cart for user
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingCartItem = cartItemRepository.findByUserIdAndProductId(user.getId(), productId);

        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException("Insufficient stock. Available: " + product.getStockQuantity() + ", Total requested: " + newQuantity);
            }

            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());
        }

        cartItem = cartItemRepository.save(cartItem);
        return convertToCartItemResponse(cartItem);
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new ResourceNotFoundException("Cart item not found with id: " + cartItemId);
        }
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Transactional
    public CartItemResponse updateCartItemQuantity(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (cartItem.getProduct().getStockQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock. Available: " + cartItem.getProduct().getStockQuantity() + ", Requested: " + quantity);
        }

        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);
        return convertToCartItemResponse(cartItem);
    }

    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        return CartItemResponse.builder()
                .cartItemId(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .productDescription(cartItem.getProduct().getDescription())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .totalPrice(cartItem.getTotalPrice())
                .build();
    }
}
