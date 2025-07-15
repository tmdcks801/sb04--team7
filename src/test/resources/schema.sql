CREATE TABLE attributes (
                            id UUID NOT NULL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL UNIQUE,
                            details VARCHAR(4000) NOT NULL,
                            created_at TIMESTAMP(6) NOT NULL,
                            updated_at TIMESTAMP(6)
);

CREATE TABLE feeds (
                       id UUID NOT NULL PRIMARY KEY,
                       user_id UUID NOT NULL,
                       weather_id UUID NOT NULL,
                       created_at TIMESTAMP(6) NOT NULL,
                       updated_at TIMESTAMP(6),
                       content VARCHAR(255),
                       like_count BIGINT,
                       comment_count INTEGER
);

CREATE TABLE feed_comments (
                               id UUID NOT NULL PRIMARY KEY,
                               feed_id UUID NOT NULL,
                               user_id UUID NOT NULL,
                               content VARCHAR(255),
                               created_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE images (
                        id UUID NOT NULL PRIMARY KEY,
                        url VARCHAR(2048) NOT NULL UNIQUE,
                        created_at TIMESTAMP(6) NOT NULL,
                        file_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE clothes (
                         id UUID NOT NULL PRIMARY KEY,
                         user_id UUID NOT NULL,
                         image_id UUID,
                         created_at TIMESTAMP(6) NOT NULL,
                         updated_at TIMESTAMP(6),
                         name VARCHAR(255) NOT NULL,
                         type VARCHAR(255) NOT NULL
);

CREATE TABLE clothes_attributes (
                                    id UUID NOT NULL PRIMARY KEY,
                                    attribute_id UUID NOT NULL,
                                    clothes_id UUID NOT NULL,
                                    value VARCHAR(255) NOT NULL UNIQUE,
                                    CONSTRAINT uq_clothes_attr UNIQUE (attribute_id, clothes_id)
);

CREATE TABLE feed_clothes (
                              id UUID NOT NULL PRIMARY KEY,
                              feed_id UUID NOT NULL,
                              clothes_id UUID NOT NULL
);

CREATE TABLE users (
                       id UUID NOT NULL PRIMARY KEY,
                       location_id UUID,
                       image_id UUID,
                       birth_date DATE,
                       gender VARCHAR(255),
                       is_locked BOOLEAN,
                       temperature_sensitivity INTEGER,
                       created_at TIMESTAMP(6),
                       updated_at TIMESTAMP(6),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       name VARCHAR(255) NOT NULL,
                       password VARCHAR(255),
                       provider VARCHAR(255),
                       provider_id VARCHAR(255),
                       role VARCHAR(255),
                       is_temp_password BOOLEAN NOT NULL,
                       temp_password_expiration TIMESTAMP(6)
);

CREATE TABLE feed_likes (
                            id UUID NOT NULL PRIMARY KEY,
                            feed_id UUID NOT NULL,
                            user_id UUID NOT NULL,
                            created_at TIMESTAMP(6) NOT NULL,
                            CONSTRAINT pk_feed_user UNIQUE (feed_id, user_id)
);

CREATE TABLE follows (
                         id UUID NOT NULL PRIMARY KEY,
                         followees UUID NOT NULL,
                         followers UUID NOT NULL,
                         created_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE messages (
                          id UUID NOT NULL PRIMARY KEY,
                          receiver UUID NOT NULL,
                          sender UUID NOT NULL,
                          created_at TIMESTAMP(6) NOT NULL,
                          contents VARCHAR(255),
                          room_key VARCHAR(255) NOT NULL,
                          dm_key VARCHAR(255) NOT NULL
);

CREATE TABLE locations (
                           id UUID NOT NULL PRIMARY KEY,
                           location_x INTEGER NOT NULL,
                           location_y INTEGER NOT NULL,
                           latitude DOUBLE,
                           longitude DOUBLE,
                           location_names VARCHAR(4000)
);

CREATE TABLE weather (
                         id UUID NOT NULL PRIMARY KEY,
                         region_name VARCHAR(255) NOT NULL,
                         humidity_compared_to_day_before DOUBLE,
                         humidity_current DOUBLE,
                         precipitation_amount DOUBLE,
                         precipitation_probability DOUBLE,
                         temperature_compared_to_day_before DOUBLE,
                         temperature_current DOUBLE,
                         temperature_max DOUBLE,
                         temperature_min DOUBLE,
                         wind_speed DOUBLE,
                         forecast_at TIMESTAMP(6),
                         forecasted_at TIMESTAMP(6),
                         precipitation_type VARCHAR(255),
                         sky_status VARCHAR(255),
                         wind_as_word VARCHAR(255)
);

CREATE TABLE jwt_session (
                             id UUID NOT NULL PRIMARY KEY,
                             user_id UUID NOT NULL,
                             access_token VARCHAR(4000) NOT NULL UNIQUE,
                             refresh_token VARCHAR(4000) NOT NULL UNIQUE
);

ALTER TABLE clothes
    ADD CONSTRAINT FK_clothes_images
        FOREIGN KEY (image_id)
            REFERENCES images (id);

ALTER TABLE clothes_attributes
    ADD CONSTRAINT FK_clothes_attr_attributes
        FOREIGN KEY (attribute_id)
            REFERENCES attributes (id);

ALTER TABLE clothes_attributes
    ADD CONSTRAINT FK_clothes_attr_clothes
        FOREIGN KEY (clothes_id)
            REFERENCES clothes (id);

ALTER TABLE feed_clothes
    ADD CONSTRAINT FK_feed_clothes_clothes
        FOREIGN KEY (clothes_id)
            REFERENCES clothes (id);

ALTER TABLE feed_clothes
    ADD CONSTRAINT FK_feed_clothes_feeds
        FOREIGN KEY (feed_id)
            REFERENCES feeds (id);

ALTER TABLE feed_comments
    ADD CONSTRAINT FK_feed_comments_feeds
        FOREIGN KEY (feed_id)
            REFERENCES feeds (id);

ALTER TABLE feed_comments
    ADD CONSTRAINT FK_feed_comments_users
        FOREIGN KEY (user_id)
            REFERENCES users (id);

ALTER TABLE feed_likes
    ADD CONSTRAINT FK_feed_likes_feeds
        FOREIGN KEY (feed_id)
            REFERENCES feeds (id);

ALTER TABLE feed_likes
    ADD CONSTRAINT FK_feed_likes_users
        FOREIGN KEY (user_id)
            REFERENCES users (id);

ALTER TABLE follows
    ADD CONSTRAINT FK_follows_users_followees
        FOREIGN KEY (followees)
            REFERENCES users (id);

ALTER TABLE follows
    ADD CONSTRAINT FK_follows_users_followers
        FOREIGN KEY (followers)
            REFERENCES users (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_messages_users_receiver
        FOREIGN KEY (receiver)
            REFERENCES users (id);

ALTER TABLE messages
    ADD CONSTRAINT FK_messages_users_sender
        FOREIGN KEY (sender)
            REFERENCES users (id);

ALTER TABLE feeds
    ADD CONSTRAINT FK_feeds_users
        FOREIGN KEY (user_id)
            REFERENCES users (id);

ALTER TABLE feeds
    ADD CONSTRAINT FK_feeds_weather
        FOREIGN KEY (weather_id)
            REFERENCES weather (id);

ALTER TABLE users
    ADD CONSTRAINT FK_users_locations
        FOREIGN KEY (location_id)
            REFERENCES locations (id);

ALTER TABLE users
    ADD CONSTRAINT FK_users_images
        FOREIGN KEY (image_id)
            REFERENCES images (id);

ALTER TABLE jwt_session
    ADD CONSTRAINT FK_jwt_session_users
        FOREIGN KEY (user_id)
            REFERENCES users (id);

CREATE INDEX idx_weather_region_name
    ON weather (region_name);

CREATE INDEX idx_location_lat_lng
    ON locations (latitude, longitude);
