package com.gap.ecommerceapp.controller;

import com.gap.ecommerceapp.dto.AddToCartRequest;
import com.gap.ecommerceapp.dto.CartResponse;
import com.gap.ecommerceapp.dto.CartItemResponse;
import com.gap.ecommerceapp.model.User;
import com.gap.ecommerceapp.service.CartService;
import com.gap.ecommerceapp.service.UserService;
import com.gap.ecommerceapp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getCartItems(@PathVariable Long userId) {
        CartResponse cartResponse = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cartResponse);
    }

    @PostMapping("/add")
    public ResponseEntity<CartItemResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        User user = userService.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        CartItemResponse cartItem = cartService.addToCart(user, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(cartItem);
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<CartItemResponse> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        CartItemResponse cartItem = cartService.updateCartItemQuantity(cartItemId, quantity);
        return ResponseEntity.ok(cartItem);
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}
