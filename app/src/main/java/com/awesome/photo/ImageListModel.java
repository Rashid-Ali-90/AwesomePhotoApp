package com.awesome.photo;

public class ImageListModel {

        private String imageURL;
        private String authorName;
        private String description;

        public String getImageURL() {
            return imageURL;
        }

        public void setImageURL(String imageURLParam) {
            imageURL = imageURLParam;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorNameParam) {
            authorName = authorNameParam;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String descriptionParam) {
            description = descriptionParam;
        }
}