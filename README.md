# Warehouse Inventory and Supplier Management System

A desktop application designed for undergraduate students to demonstrate Core Java OOP principles, Java Collections, Stream API, Exception Handling, and Swing GUI development.

---

## 1. Project Overview
- **Technology Stack:** Core Java (JDK 17 or higher)
- **Graphical User Interface:** Java Swing (`JFrame`, `JTabbedPane`, `JPanel`, `JTable`, `DefaultTableModel`, `JOptionPane`)
- **Data Persistence:** Google Gson (Flat JSON files stored in `data/`)
- **IDE Compatibility:** IntelliJ IDEA, Eclipse, VS Code, or Command Line

---

## 2. Team Division & File Ownership (6 Members)

| Member | Role | Owned Files | Core Syllabus Concepts |
| :--- | :--- | :--- | :--- |
| **Member 1** | **Product & Validation Lead** | `Product.java`<br>`ProductService.java`<br>`InputValidator.java`<br>`ProductNotFoundException.java`<br>`DuplicateProductException.java` | Custom Checked Exceptions, Constructor Overloading, Validation, Encapsulation |
| **Member 2** | **Supplier & OOP Hierarchy Lead** | `BaseEntity.java` (Abstract)<br>`Supplier.java`<br>`SupplierService.java` | Abstraction, Inheritance (`extends`, `super`), Method Overriding, `HashSet` uniqueness, Referential Integrity |
| **Member 3** | **Inventory & Transactions Lead** | `Transaction.java`<br>`InventoryService.java`<br>`InsufficientStockException.java` | `LinkedList`, `Iterator`, `StringBuilder`, Custom Business Logic Exceptions |
| **Member 4** | **Purchase Orders & Reports Lead** | `OrderStatus.java` (Enum)<br>`PurchaseOrder.java`<br>`PurchaseOrderService.java`<br>`ReportService.java` | Enums, State Transitions, Java **Stream API** (`filter`, `mapToDouble`, `sum`), Lambdas, `TreeSet` with `Comparator`, `HashMap` |
| **Member 5** | **JSON Storage & Generics Lead** | `StorageService.java` (Generic Interface)<br>`DataManager.java` | Generic Interface (`<T>`), Gson `TypeToken`, `try-catch-finally` resource handling, JSON persistence |
| **Member 6** | **Main Function, UI & Integration Lead** | `Main.java`<br>`MainFrame.java`<br>`DashboardPanel.java`<br>`ProductPanel.java`<br>`SupplierPanel.java`<br>`InventoryPanel.java`<br>`OrderPanel.java` | Dependency Injection, Swing EDT (`SwingUtilities.invokeLater`), `JTabbedPane`, `JTable`, Event-driven programming |

---

## 3. Project Directory Structure
```text
java_project/
|-- bin/                     (Compiled .class bytecode)
|-- data/                    (JSON storage files)
|   |-- products.json
|   |-- suppliers.json
|   |-- orders.json
|   |-- transactions.json
|-- lib/
|   |-- gson-2.10.1.jar      (Google Gson Library)
|-- src/
|   |-- exceptions/
|   |   |-- DuplicateProductException.java
|   |   |-- InsufficientStockException.java
|   |   |-- ProductNotFoundException.java
|   |-- main/
|   |   |-- Main.java
|   |-- models/
|   |   |-- BaseEntity.java
|   |   |-- OrderStatus.java
|   |   |-- Product.java
|   |   |-- PurchaseOrder.java
|   |   |-- Supplier.java
|   |   |-- Transaction.java
|   |-- services/
|   |   |-- InventoryService.java
|   |   |-- ProductService.java
|   |   |-- PurchaseOrderService.java
|   |   |-- ReportService.java
|   |   |-- SupplierService.java
|   |-- storage/
|   |   |-- DataManager.java
|   |   |-- StorageService.java
|   |-- ui/
|   |   |-- DashboardPanel.java
|   |   |-- InventoryPanel.java
|   |   |-- MainFrame.java
|   |   |-- OrderPanel.java
|   |   |-- ProductPanel.java
|   |   |-- SupplierPanel.java
|   |-- utils/
|       |-- InputValidator.java
|-- compile.bat              (One-click compilation script)
|-- run.bat                  (One-click launch script)
|-- README.md
```

---

## 4. How to Run the Application

### Option A: Command Line / Batch Script (Windows)
Double-click `run.bat` or run in terminal:
```powershell
.\run.bat
```

Or manually:
```powershell
javac -cp "lib\gson-2.10.1.jar" -d bin src\models\*.java src\exceptions\*.java src\utils\*.java src\storage\*.java src\services\*.java src\ui\*.java src\main\*.java
java -cp "bin;lib\gson-2.10.1.jar" main.Main
```

### Option B: IntelliJ IDEA
1. Open folder in IntelliJ IDEA.
2. Go to **File -> Project Structure -> Libraries** -> Click `+` -> Select `lib/gson-2.10.1.jar`.
3. Right click `src/main/Main.java` -> Select **Run 'Main.main()'**.

### Option C: Eclipse
1. Create new Java Project, set source directory to `src`.
2. Right click project -> **Build Path -> Add External Archives** -> Select `lib/gson-2.10.1.jar`.
3. Run `Main.java` as Java Application.

---

## 5. Viva Preparation & Defense Guide

### Member 1: Product & Validation Lead
- **Q: Why are your custom exceptions checked exceptions?**
  *A:* They extend `java.lang.Exception`. Checked exceptions require explicit handling (`try-catch` or `throws`) at compile time, guaranteeing that callers handle missing products or duplicate IDs gracefully.
- **Q: What is Constructor Overloading?**
  *A:* Providing multiple constructors in `Product.java` with different parameter signatures (e.g. one with initial quantity and one defaulting to 0).
- **Q: Why is InputValidator's constructor private?**
  *A:* It is a utility class containing only `static` methods. A private constructor prevents unnecessary object instantiation.

### Member 2: Supplier & OOP Hierarchy Lead
- **Q: What is the purpose of the abstract class BaseEntity?**
  *A:* It provides common attributes (`id`, `name`) and declares the abstract method `toTableRow()`, forcing subclasses (`Product`, `Supplier`) to provide their own table formatting.
- **Q: Why use `HashSet` in SupplierService?**
  *A:* A `HashSet` stores unique elements with average $O(1)$ lookup time, making duplicate ID detection efficient.
- **Q: How is referential integrity protected when deleting a supplier?**
  *A:* `SupplierService.deleteSupplier()` queries `ProductService.hasProductWithSupplier()`. If any product references that `supplierId`, it throws an `IllegalStateException` to prevent orphan product records.

### Member 3: Inventory & Transactions Lead
- **Q: Why use a `LinkedList` for transaction history?**
  *A:* `LinkedList` allows $O(1)$ constant time insertion at the beginning (`addFirst()`), so the newest transaction events immediately appear at the top of the history.
- **Q: Why use `StringBuilder` instead of `+` concatenation?**
  *A:* In Java, `String` is immutable. Repeated concatenation in loops creates many temporary objects in memory. `StringBuilder` modifies an internal buffer in place.
- **Q: When is InsufficientStockException thrown?**
  *A:* In `stockOut()`, if the requested deduction exceeds the product's current on-hand quantity, preventing negative warehouse inventory.

### Member 4: Purchase Orders & Reports Lead
- **Q: Why use an Enum for OrderStatus instead of Strings?**
  *A:* Enums guarantee compile-time type safety, preventing invalid statuses like `"DONE"` or `"PENDINGG"`.
- **Q: How does ReportService use the Stream API?**
  *A:* `getLowStockProducts()` filters products using `.filter(p -> p.getQuantity() < threshold)` and collects them with `Collectors.toList()`. `calculateTotalInventoryValue()` uses `.mapToDouble(p -> p.getPrice() * p.getQuantity()).sum()`.
- **Q: Why use TreeSet with a custom Comparator?**
  *A:* `TreeSet` automatically keeps elements sorted. The custom `Comparator` sorts products by price or quantity and uses `id` as a tie-breaker so products with identical prices aren't lost.

### Member 5: JSON Storage & Generics Lead
- **Q: What is the benefit of the generic interface `StorageService<T>`?**
  *A:* It decouples file storage from specific model classes, allowing the same loading and saving signatures to work with any entity type while enforcing compile-time type safety.
- **Q: What is Gson TypeToken and why is it needed?**
  *A:* Due to Java Generic Type Erasure, generic types like `List<Product>` lose their specific type information at runtime. Gson's `TypeToken` captures and preserves the generic type so JSON arrays are correctly deserialized into typed objects.
- **Q: Why use `try-catch-finally`?**
  *A:* The `finally` block is guaranteed to execute regardless of whether an exception occurs, ensuring that file descriptors and streams are properly closed to avoid file locks or resource leaks.

### Member 6: Main Function, UI & Integration Lead
- **Q: Why launch the GUI with `SwingUtilities.invokeLater()`?**
  *A:* Swing components are not thread-safe and must be constructed and updated on the Event Dispatch Thread (EDT) to avoid UI rendering race conditions.
- **Q: How is tab synchronization achieved?**
  *A:* `MainFrame` attaches a `ChangeListener` to `JTabbedPane`. Whenever the user switches tabs, `refreshData()` is called on the active panel so modifications made in one tab immediately reflect in the others.

