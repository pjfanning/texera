USE texera_db;

DROP TABLE IF EXISTS dataset_user_likes;

CREATE TABLE IF NOT EXISTS dataset_user_likes
(
    `uid` INT UNSIGNED NOT NULL,
    `did` INT UNSIGNED NOT NULL,
    PRIMARY KEY (`uid`, `did`),
    FOREIGN KEY (`uid`) REFERENCES `user` (`uid`) ON DELETE CASCADE,
    FOREIGN KEY (`did`) REFERENCES `dataset` (`did`) ON DELETE CASCADE
    ) ENGINE = INNODB;