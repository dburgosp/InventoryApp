# Inventory App
This is a simple Android Studio project for the [Android Basics Nanodegree](https://www.udacity.com/course/android-basics-nanodegree-by-google--nd803) by Udacity and Google. It implements a very basic and limited functionality (only for educational purpose) and uses a sample SQLite database with a single table. 

Some screen captures:

<IMG src="https://github.com/dburgosp/InventoryApp/blob/master/img_empty_database.jpg?raw=true" width="150" height="279" title="Empty Database" alt="Empty Database"/> <IMG src="https://github.com/dburgosp/InventoryApp/blob/master/img_new_product.jpg?raw=true" width="150" height="279" title="New product" alt="New product"/> <IMG src="https://github.com/dburgosp/InventoryApp/blob/master/img_products_list.jpg?raw=true" width="150" height="279" title="Product list" alt="Product list"/> <IMG src="https://github.com/dburgosp/InventoryApp/blob/master/img_edit_product.jpg?raw=true" width="150" height="279" title="Edit product" alt="Edit product"/> <IMG src="https://github.com/dburgosp/InventoryApp/blob/master/img_exit_editing.jpg?raw=true" width="150" height="279" title="Closing edit layout" alt="Closing edit layout"/> <IMG src="https://github.com/dburgosp/InventoryApp/blob/master/img_supplier_order.jpg?raw=true" width="150" height="279" title="Order to supplier" alt="Order to supplier"/> <IMG src="https://github.com/dburgosp/InventoryApp/blob/master/img_delete_all.jpg?raw=true" width="150" height="279" title="Delete all products" alt="Delete all products"/> <IMG src="https://github.com/dburgosp/InventoryApp/blob/master/img_delete_product.jpg?raw=true" width="150" height="279" title="Delete one product" alt="Delete one product"/>

## Project Overview
This project is a chance for you to combine and practice everything you learned in this section of the Nanodegree program. You will be making an app to track a store's inventory.

The goal is to design and create the structure of an Inventory App which would allow a store to keep track of its inventory of products. The app will need to store information about price, quantity available, supplier, and a picture of the product. It will also need to allow the user to track sales and shipments and make it easy for the user to order more from the listed supplier.

## Layout

1. **Overall Layout**. The app contains a list of current products and a button to add a new product.
2. **List Item Layout**. Each list item displays the product name, current quantity, and price. Each list item also contains a Sale Button that reduces the quantity by one (include logic so that no negative quantities are displayed).
3. **Detail Layout**:
   * The Detail Layout for each item displays the remainder of the information stored in the database.
   * The Detail Layout contains buttons that increase and decrease the available quantity displayed.
   * The Detail Layout contains a button to order from the supplier.
   * The detail view contains a button to delete the product record entirely.
4. **Layout Best Practices**. The code adheres to all of the following best practices:
   * Text sizes are defined in sp.
   * Lengths are defined in dp.
   * Padding and margin is used appropriately, such that the views are not crammed up against each other.
5. **Default Textview**. When there is no information to display in the database, the layout displays a TextView with instructions on how to populate the database.

## Functionality
1. **Runtime Errors**. The code runs without errors. For example, when user inputs product information (quantity, price, name, image), instead of erroring out, the app includes logic to validate that no null values are accepted. If a null value is inputted, add a Toast that prompts the user to input the correct information before they can continue.
2. **ListView Population**. The listView populates with the current products stored in the table.
3. **Add product button**. The Add product button prompts the user for information about the product and a picture, each of which are then properly stored in the table.
4. **Input Validation**. User input is validated. In particular, empty product information is not accepted. If user inputs product information (quantity, price, name, image), instead of erroring out, the app includes logic to validate that no null values are accepted. If a null value is inputted, add a Toast that prompts the user to input the correct information before they can continue.
5. **Sale Button**. In the activity that displays a list of all available inventory, each List Item contains a Sale Button which reduces the available quantity for that particular product by one (include logic so that no negative quantities are displayed).
6. **Detail View Intent**. Clicking on the rest of each list item sends the user to the detail screen for the correct product.
7. **Modify Quantity Buttons**. The Modify Quantity Buttons in the detail view properly increase and decrease the quantity available for the correct product. The student may also add input for how much to increase or decrease the quantity by.
8. **Order Button**. The ‘order more’ button sends an intent to either a phone app or an email app to contact the supplier using the information stored in the database.
9. **Delete Button**. The delete button prompts the user for confirmation and, if confirmed, deletes the product record entirely and sends the user back to the main activity.
10. **External Libraries and Packages**. The intent of this project is to give you practice writing raw Java code using the necessary classes provided by the Android framework; therefore, the use of external libraries for core functionality will not be permitted to complete this project.

## Code Readability
1. **Naming Conventions**. All variables, methods, and resource IDs are descriptively named such that another developer reading the code can easily understand their function.
2. **Format**. The code is properly formatted i.e. there are no unnecessary blank lines; there are no unused variables or methods; there is no commented out code. The code also has proper indentation when defining variables and methods.

## Notes
Despite of point 10 in section "Functionality" (the use of external libraries for core functionality will not be permitted to complete this project) but following the guidelines of the course mentors, I have used [Butterknife library](http://jakewharton.github.io/butterknife/) for view injection in order to make my code less and more clear.
