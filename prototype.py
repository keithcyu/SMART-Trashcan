from keras.preprocessing.image import load_img, img_to_array
from keras.applications.vgg16 import VGG16, preprocess_input, decode_predictions

### Reference: https://machinelearningmastery.com/use-pre-trained-vgg-model-classify-objects-photographs/


# load pretrained model
model = VGG16()

# load an image from file
image = load_img('plastic.jpg', target_size=(224, 224))

# convert the imgae pixels to a numpy array
image = img_to_array(image)

# reshape data for the model
image = image.reshape((1, image.shape[0], image.shape[1], image.shape[2]))

# predict the probability across all output classes
yhat = model.predict(image)

# convert the probabilities to class labels
label = decode_predictions(yhat)
# retrieve the most likely result, e.g. highest probability
label = label[0][0]
# print the classification
print('%s (%.2f%%)' % (label[1], label[2]*100))

