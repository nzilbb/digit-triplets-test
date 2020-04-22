CREATE TABLE form_field (
  field varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '',
  name varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '',
  description varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '',
  type varchar(20) NOT NULL default '',
  size varchar(20) NOT NULL default '',
  required INT(10) NOT NULL default 1,
  display_order int(11) NOT NULL default 0,
  update_date datetime NULL,
  update_user_id varchar(100) NOT NULL,
  PRIMARY KEY  (field)
);

CREATE TABLE form_field_option (
  field varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '',
  value varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '',
  description varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '',
  display_order int(11) NOT NULL default 0,
  update_date datetime NULL,
  update_user_id varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY  (field,value)
);

CREATE TABLE instance (
  instance_id INTEGER NOT NULL AUTO_INCREMENT,
  user_agent varchar(255) NOT NULL default '',
  ip varchar(30) NOT NULL default '',
  start_time datetime NULL,
  end_time datetime NULL,
  trial_set_number INTEGER NULL,
  test_result INTEGER NULL,
  mean_snr DOUBLE NULL,
  mode varchar(1) NOT NULL default '',
  trials_csv TEXT,
  PRIMARY KEY  (instance_id)
);

CREATE TABLE instance_field (
  instance_id INTEGER NOT NULL,
  field varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '',
  value varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL default '',
  PRIMARY KEY  (instance_id, field)
);

CREATE TABLE trial (
  instance_id INTEGER NOT NULL AUTO_INCREMENT,
  trial_number INTEGER NOT NULL,
  correct_answer VARCHAR(10) NOT NULL,
  decibels_signal INTEGER NULL,
  participant_answer VARCHAR(10) NULL,
  PRIMARY KEY  (instance_id, trial_number)
);
