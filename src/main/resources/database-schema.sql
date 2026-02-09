-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- CHARGED WEB SYSTEM - DATABASE SCHEMA
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- Web Matches Table (detailed match data for web)
CREATE TABLE IF NOT EXISTS web_matches (
    match_id VARCHAR(50) PRIMARY KEY,
    player1_uuid VARCHAR(36) NOT NULL,
    player2_uuid VARCHAR(36) NOT NULL,
    player1_name VARCHAR(16) NOT NULL,
    player2_name VARCHAR(16) NOT NULL,
    
    mode VARCHAR(32) NOT NULL,
    match_type VARCHAR(32) NOT NULL,
    arena VARCHAR(64) NOT NULL,
    
    winner_uuid VARCHAR(36),
    duration_seconds INT NOT NULL,
    
    -- ELO data
    player1_elo_before INT NOT NULL,
    player1_elo_after INT NOT NULL,
    player1_elo_change INT NOT NULL,
    player2_elo_before INT NOT NULL,
    player2_elo_after INT NOT NULL,
    player2_elo_change INT NOT NULL,
    
    -- Statistics JSON
    player1_stats JSON NOT NULL,
    player2_stats JSON NOT NULL,
    
    -- Timeline JSON
    match_timeline JSON,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    views INT DEFAULT 0,
    shared_count INT DEFAULT 0,
    
    INDEX idx_player1 (player1_uuid),
    INDEX idx_player2 (player2_uuid),
    INDEX idx_mode (mode),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Player Stats Extended (for web profiles)
CREATE TABLE IF NOT EXISTS web_player_stats (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    
    -- Global stats
    total_matches INT DEFAULT 0,
    total_wins INT DEFAULT 0,
    total_losses INT DEFAULT 0,
    
    -- Per-mode stats (JSON for flexibility)
    nodebuff_stats JSON,
    gapple_stats JSON,
    sumo_stats JSON,
    builduhc_stats JSON,
    combo_stats JSON,
    debuff_stats JSON,
    
    -- Achievements
    achievements JSON,
    
    -- Profile customization
    custom_banner VARCHAR(255),
    bio TEXT,
    favorite_mode VARCHAR(32),
    
    -- Privacy
    profile_public BOOLEAN DEFAULT TRUE,
    hide_stats BOOLEAN DEFAULT FALSE,
    
    -- Metadata
    first_match_at TIMESTAMP,
    last_match_at TIMESTAMP,
    profile_views INT DEFAULT 0,
    
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_updated (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Match Analytics (detailed tracking)
CREATE TABLE IF NOT EXISTS match_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    match_id VARCHAR(50) NOT NULL,
    player_uuid VARCHAR(36) NOT NULL,
    
    -- Combat data
    hits_given INT DEFAULT 0,
    hits_taken INT DEFAULT 0,
    max_combo INT DEFAULT 0,
    total_combos INT DEFAULT 0,
    
    -- Accuracy
    hit_accuracy DECIMAL(5,2) DEFAULT 0.00,
    critical_hit_percentage DECIMAL(5,2) DEFAULT 0.00,
    
    -- CPS tracking
    average_cps DECIMAL(5,2) DEFAULT 0.00,
    max_cps DECIMAL(5,2) DEFAULT 0.00,
    
    -- Potion usage
    potions_used INT DEFAULT 0,
    potions_missed INT DEFAULT 0,
    pot_accuracy DECIMAL(5,2) DEFAULT 0.00,
    
    -- Movement
    distance_traveled DECIMAL(10,2) DEFAULT 0.00,
    sprint_percentage DECIMAL(5,2) DEFAULT 0.00,
    
    -- Build data (for BuildUHC)
    blocks_placed INT DEFAULT 0,
    blocks_broken INT DEFAULT 0,
    
    -- Network
    average_ping INT DEFAULT 0,
    ping_spikes INT DEFAULT 0,
    
    -- Timing
    reaction_time_ms INT DEFAULT 0,
    
    FOREIGN KEY (match_id) REFERENCES web_matches(match_id) ON DELETE CASCADE,
    INDEX idx_match (match_id),
    INDEX idx_player (player_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Leaderboards (cached for performance)
CREATE TABLE IF NOT EXISTS web_leaderboards (
    id INT PRIMARY KEY AUTO_INCREMENT,
    category VARCHAR(32) NOT NULL,
    mode VARCHAR(32),
    
    player_uuid VARCHAR(36) NOT NULL,
    player_name VARCHAR(16) NOT NULL,
    
    rank_position INT NOT NULL,
    score INT NOT NULL,
    
    division VARCHAR(32),
    badge VARCHAR(255),
    
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_rank (category, mode, rank_position),
    INDEX idx_category (category),
    INDEX idx_player (player_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Staff Members (synced from Discord)
CREATE TABLE IF NOT EXISTS web_staff (
    id INT PRIMARY KEY AUTO_INCREMENT,
    uuid VARCHAR(36) UNIQUE NOT NULL,
    username VARCHAR(16) NOT NULL,
    
    role VARCHAR(32) NOT NULL,
    role_priority INT DEFAULT 0,
    
    discord_id VARCHAR(32),
    discord_username VARCHAR(64),
    
    status VARCHAR(16) DEFAULT 'offline',
    
    bio TEXT,
    avatar_url VARCHAR(255),
    
    join_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP,
    
    INDEX idx_role (role),
    INDEX idx_priority (role_priority DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Page Analytics
CREATE TABLE IF NOT EXISTS web_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    page_type VARCHAR(32) NOT NULL,
    page_id VARCHAR(64),
    
    visitor_ip VARCHAR(45),
    user_agent TEXT,
    referrer TEXT,
    
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_page (page_type, page_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- API Keys
CREATE TABLE IF NOT EXISTS web_api_keys (
    id INT PRIMARY KEY AUTO_INCREMENT,
    api_key VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(64) NOT NULL,
    
    permissions JSON,
    
    rate_limit_per_minute INT DEFAULT 60,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    expires_at TIMESTAMP,
    
    active BOOLEAN DEFAULT TRUE,
    
    INDEX idx_key (api_key),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;