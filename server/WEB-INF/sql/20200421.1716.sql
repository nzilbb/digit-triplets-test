CREATE TABLE version (
  version varchar(50) NOT NULL default '',
  upgraded TIMESTAMP,
  PRIMARY KEY  (version)
) ENGINE=MyISAM;

CREATE TABLE user (
  user varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  password varchar(100) NOT NULL default '',
  reset_password tinyint NOT NULL default 0,
  email VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  expiry DATETIME NULL,
  PRIMARY KEY  (user)
) ENGINE=MyISAM; 

CREATE TABLE role (
  user varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  role varchar(50) NOT NULL default '',
  PRIMARY KEY  (user,role)
) ENGINE=MyISAM; 

/* create initial user */
INSERT INTO user (user) VALUES ('admin');
INSERT INTO role (user, role) VALUES ('admin', 'admin');
