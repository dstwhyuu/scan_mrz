import cv2
import numpy as np

# Create a larger blank white image (more realistic passport aspect ratio)
img = np.ones((800, 600, 3), dtype=np.uint8) * 255

# Define the MRZ text
mrz_line1 = "P<IDNSANTOSO<<BUDI<<<<<<<<<<<<<<<<<<<<<<<<<<"
mrz_line2 = "A1234567<8IDN9005108M2905107<<<<<<<<<<<<<<02"

# Use OCR-B like font
font = cv2.FONT_HERSHEY_SIMPLEX
font_scale = 0.7
font_thickness = 2
color = (0, 0, 0) # Black

# Starting position for the text in the bottom 20%
x = 20
y = 700

# Put text on the image
cv2.putText(img, mrz_line1, (x, y), font, font_scale, color, font_thickness, cv2.LINE_AA)
cv2.putText(img, mrz_line2, (x, y + 40), font, font_scale, color, font_thickness, cv2.LINE_AA)

# Save the image
cv2.imwrite("dummy_passport.jpg", img)
print("Dummy passport MRZ image generated: dummy_passport.jpg")
