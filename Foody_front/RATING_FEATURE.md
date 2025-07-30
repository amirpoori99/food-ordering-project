# Rating Feature for Completed Orders

## Overview
This feature allows buyers to rate their completed orders in the buyer dashboard. The rating system provides a way for customers to provide feedback on their food delivery experience.

## Features

### 1. Rate Button in Order History
- A "⭐ Rate" button appears in the order history table for orders with "completed" status
- The button is styled with a green background and star icon to make it visually appealing
- Only completed orders show the rate button

### 2. Rating Dialog
- Clicking the rate button opens a modal dialog
- Users can select a rating from 1-5 stars
- Optional comment field for additional feedback
- Clean, modern UI design consistent with the application theme

### 3. API Integration
- Uses the `/ratings` endpoint as defined in the API specification
- Sends rating data in the correct format:
  - `order_id`: The order ID
  - `rating`: Integer value (1-5)
  - `comment`: Optional string comment

## Implementation Details

### Files Added/Modified

#### New Files:
- `src/main/resources/com/foodi/appFrontend/view/dashbord/RatingDialogView.fxml` - Rating dialog UI
- `src/main/java/com/foodi/appFrontend/tabs/dashbord/RatingDialogController.java` - Rating dialog controller

#### Modified Files:
- `src/main/resources/com/foodi/appFrontend/view/dashbord/BuyerDashboardView.fxml` - Added rate column to order history table
- `src/main/java/com/foodi/appFrontend/tabs/dashbord/BuyerDashboard.java` - Added rate column and rating functionality

### Key Components

1. **Rate Column**: Added to the order history table with a custom cell factory
2. **Button Cell Factory**: Creates rate buttons only for completed orders
3. **Rating Dialog**: Modal dialog for collecting rating and comment
4. **API Integration**: Handles rating submission to the backend

## Usage

1. Navigate to the "Order History" tab in the buyer dashboard
2. Look for orders with "completed" status
3. Click the "⭐ Rate" button in the "Rate" column
4. Select a rating (1-5 stars) from the dropdown
5. Optionally add a comment about your experience
6. Click "Submit Rating" to save your feedback

## Error Handling

- Validates that a rating is selected before submission
- Shows appropriate error messages for authentication issues
- Handles network errors gracefully
- Provides user feedback on successful rating submission

## API Endpoint

The feature uses the `/ratings` POST endpoint with the following structure:

```json
{
  "order_id": 123,
  "rating": 5,
  "comment": "Great food and fast delivery!"
}
```

## Future Enhancements

Potential improvements for the rating feature:
- Add image upload capability for food photos
- Show existing ratings for orders that have already been rated
- Add rating history view
- Implement rating analytics for restaurants
- Add rating notifications for restaurants 