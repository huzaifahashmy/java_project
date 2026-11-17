package services;

import models.Product;
import models.Supplier;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * TEAM MEMBER 4: PURCHASE ORDERS & REPORTS LEAD
 * File: ReportService.java
 *
 * SYLLABUS CONCEPTS DEMONSTRATED:
 * 1. Java Stream API:
 *    - stream().filter(...) with Lambda expressions for low-stock filtering.
 *    - stream().mapToDouble(...).sum() for inventory financial valuation.
 * 2. Lambda Expressions & Method References: Concise functional syntax.
 * 3. Comparator Interface & TreeSet: Sorts products dynamically by price or
 *    quantity while preserving all entries through tie-breaking logic.
 * 4. Java Collections - HashMap: Aggregates counts of products per supplier.
 * ==============================================================================
 */
public class ReportService {

    private final ProductService productService;
    private final SupplierService supplierService;

    /**
     * Constructor injecting ProductService and SupplierService.
     *
     * @param productService  Product service
     * @param supplierService Supplier service
     */
    public ReportService(ProductService productService, SupplierService supplierService) {
        this.productService = productService;
        this.supplierService = supplierService;
    }

    /**
     * Returns total count of registered products.
     *
     * @return count of products
     */
    public int getTotalProductsCount() {
        return productService.getAllProducts().size();
    }

    /**
     * Returns total count of registered suppliers.
     *
     * @return count of suppliers
     */
    public int getTotalSuppliersCount() {
        return supplierService.getAllSuppliers().size();
    }

    /**
     * Calculates the total inventory financial valuation of the warehouse:
     * sum of (price * quantity) across all products.
     *
     * SYLLABUS CONCEPT: Stream API (mapToDouble + sum)
     *
     * @return Total dollar valuation of inventory
     */
    public double calculateTotalInventoryValue() {
        // Stream pipeline:
        // 1. Convert List<Product> to Stream<Product>
        // 2. mapToDouble converts each Product to a primitive double value (price * qty)
        // 3. sum() aggregates all values
        return productService.getAllProducts()
                .stream()
                .mapToDouble(product -> product.getPrice() * product.getQuantity())
                .sum();
    }

    /**
     * Filters all products with stock quantity below the designated threshold (default: 10).
     *
     * SYLLABUS CONCEPT: Stream API (filter + Lambda Expression + Collectors.toList)
     *
     * @param threshold Stock limit below which an alert is triggered (e.g., 10)
     * @return List of low-stock Product entities
     */
    public List<Product> getLowStockProducts(int threshold) {
        // Stream pipeline:
        // 1. stream(): creates sequential stream
        // 2. filter(p -> p.getQuantity() < threshold): predicate lambda filtering low stock
        // 3. collect(Collectors.toList()): gathers matching elements back into a List
        return productService.getAllProducts()
                .stream()
                .filter(p -> p.getQuantity() < threshold)
                .collect(Collectors.toList());
    }

    /**
     * Sorts products by price using Comparator and TreeSet.
     *
     * SYLLABUS CONCEPT: TreeSet & Comparator with Lambda
     * NOTE FOR VIVA: A standard TreeSet considers elements with compare() == 0 as duplicates.
     * To prevent distinct products with identical prices from being excluded, we use the
     * product ID as a secondary tie-breaker comparison!
     *
     * @param ascending True for low-to-high, false for high-to-low
     * @return Set of sorted products
     */
    public Set<Product> getProductsSortedByPrice(boolean ascending) {
        Comparator<Product> priceComparator = (p1, p2) -> {
            int result = Double.compare(p1.getPrice(), p2.getPrice());
            if (result != 0) {
                return ascending ? result : -result;
            }
            // Secondary tie-breaker by ID to prevent dropping products with identical price
            return p1.getId().compareToIgnoreCase(p2.getId());
        };

        TreeSet<Product> sortedSet = new TreeSet<>(priceComparator);
        sortedSet.addAll(productService.getAllProducts());
        return sortedSet;
    }

    /**
     * Sorts products by available quantity using Comparator and TreeSet.
     *
     * SYLLABUS CONCEPT: TreeSet & Comparator with Lambda
     *
     * @param ascending True for lowest stock first, false for highest stock first
     * @return Set of sorted products
     */
    public Set<Product> getProductsSortedByQuantity(boolean ascending) {
        Comparator<Product> qtyComparator = (p1, p2) -> {
            int result = Integer.compare(p1.getQuantity(), p2.getQuantity());
            if (result != 0) {
                return ascending ? result : -result;
            }
            // Tie-breaker by ID
            return p1.getId().compareToIgnoreCase(p2.getId());
        };

        TreeSet<Product> sortedSet = new TreeSet<>(qtyComparator);
        sortedSet.addAll(productService.getAllProducts());
        return sortedSet;
    }

    /**
     * Counts how many products each supplier provides.
     *
     * SYLLABUS CONCEPT: Java Collections Framework - HashMap
     * Demonstrates key-value storage where:
     * - Key: Supplier ID
     * - Value: Number of catalog products linked to that supplier
     *
     * @return Map of Supplier ID -> Product Count
     */
    public Map<String, Integer> getProductCountPerSupplier() {
        Map<String, Integer> countMap = new HashMap<>();

        // Loop over all products and tally counts per supplierId
        for (Product product : productService.getAllProducts()) {
            String supplierId = product.getSupplierId();
            if (supplierId == null || supplierId.trim().isEmpty()) {
                supplierId = "UNASSIGNED";
            }
            int currentCount = countMap.getOrDefault(supplierId, 0);
            countMap.put(supplierId, currentCount + 1);
        }

        return countMap;
    }
}

