import java.util.HashMap;
import java.util.Map;

public class WaterIntakeHandler {
    public Map<String, Object> handleRequest(Map<String, Object> event) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String body = (String) event.get("body");
            
            // Parse the JSON body
            String gender = "male";
            String mode = "cups";
            double intake = 0;
            String bottleSize = "1"; // Default bottle size
            
            if (body != null) {
                // Parse gender
                if (body.contains("\"gender\":\"male\"")) {
                    gender = "male";
                } else if (body.contains("\"gender\":\"female\"")) {
                    gender = "female";
                }
                
                // Parse mode (cups or bottle)
                if (body.contains("\"mode\":\"cups\"")) {
                    mode = "cups";
                } else if (body.contains("\"mode\":\"bottle\"")) {
                    mode = "bottle";
                }
                
                // Parse intake value
                if (body.contains("\"intake\":")) {
                    String intakeStr = body.split("\"intake\":")[1].split("[,\\} ]")[0];
                    intake = Double.parseDouble(intakeStr);
                }
                
                // Parse bottle size if mode is bottle
                if (mode.equals("bottle") && body.contains("\"bottleSize\":")) {
                    String bottleSizeStr = body.split("\"bottleSize\":")[1].split("[,\\} ]")[0];
                    bottleSize = bottleSizeStr.replace("\"", "");
                }
            }
            
            double threshold;
            double totalLiters;
            
            if (mode.equals("cups")) {
                // Cups mode: 1 cup = 0.25 liters
                totalLiters = intake * 0.25;
                threshold = gender.equals("male") ? 3.7 : 2.7; // Convert to liters
            } else {
                // Bottle mode: intake is already in liters
                totalLiters = intake;
                threshold = gender.equals("male") ? 3.7 : 2.7;
            }
            
            // Generating message
            String message;
            if (totalLiters >= threshold) {
                message = String.format("🎉 You're doing great! You've consumed around %.2fL today.", totalLiters);
            } else {
                message = String.format("💧 You should drink more water. Aim for %.1fL daily. You had around %.2fL today.", 
                    threshold, totalLiters);
            }
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            response.put("headers", headers);
            
            // Building complete response
            String responseBody = String.format(
                "{\"message\":\"%s\",\"threshold\":%.1f,\"totalLiters\":%.2f,\"intake\":%.1f,\"gender\":\"%s\",\"mode\":\"%s\",\"success\":true}",
                message.replace("\"", "\\\""), // Escape quotes in message
                threshold, 
                totalLiters,
                intake,
                gender,
                mode
            );
            
            if (mode.equals("bottle")) {
                // Adding bottle size to response
                responseBody = responseBody.substring(0, responseBody.length() - 1) + 
                              String.format(",\"bottleSize\":\"%s\"}", bottleSize);
            }
            
            response.put("statusCode", 200);
            response.put("body", responseBody);
            
        } catch (Exception e) {
            // Error responses
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            response.put("headers", headers);
            
            response.put("statusCode", 500);
            response.put("body", "{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
        
        return response;
    }
}