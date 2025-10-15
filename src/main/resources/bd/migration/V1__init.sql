-- USERS
create table if not exists users (
  id uuid primary key,
  name varchar(120) not null,
  email varchar(160) not null unique
);

-- CATEGORIES
create table if not exists categories (
  id uuid primary key,
  user_id uuid not null references users(id) on delete cascade,
  name varchar(100) not null,
  unique(user_id, name)
);

-- STORES
create table if not exists stores (
  id uuid primary key,
  user_id uuid not null references users(id) on delete cascade,
  name varchar(140) not null,
  location varchar(180),
  unique(user_id, name)
);

-- PRODUCTS
create table if not exists products (
  id uuid primary key,
  user_id uuid not null references users(id) on delete cascade,
  category_id uuid references categories(id) on delete set null,
  store_id uuid references stores(id) on delete set null,
  name varchar(160) not null,
  brand varchar(120),
  quantity int not null default 0,
  min_stock int not null default 0,
  acquisition_date date,
  price numeric(12,2),
  image_url varchar(300),
  unique(user_id, name, brand)
);
create index if not exists idx_products_user_cat on products(user_id, category_id);

-- MOVEMENTS
-- Si mapeas enum como STRING, usa varchar:
-- type varchar(20) check (type in ('PURCHASE','CONSUMPTION'))
create table if not exists movements (
  id uuid primary key,
  product_id uuid not null references products(id) on delete cascade,
  type varchar(20) not null check (type in ('PURCHASE','CONSUMPTION')),
  quantity int not null,
  unit_price numeric(12,2),
  occurred_at timestamptz not null default now(),
  note varchar(280)
);
create index if not exists idx_movements_product_time on movements(product_id, occurred_at desc);

-- SHOPPING ITEMS
create table if not exists shopping_items (
  id uuid primary key,
  product_id uuid not null references products(id) on delete cascade,
  quantity int not null,
  is_purchased boolean not null default false,
  source varchar(20) not null check (source in ('MANUAL','AUTO_RULE')),
  created_at timestamptz not null default now()
);
create index if not exists idx_shopping_active on shopping_items(product_id) where is_purchased = false;

-- ALERTS
create table if not exists alerts (
  id uuid primary key,
  user_id uuid not null references users(id) on delete cascade,
  product_id uuid references products(id) on delete cascade,
  type varchar(20) not null check (type in ('LOW_STOCK','EXPIRY')),
  message varchar(240) not null,
  created_at timestamptz not null default now(),
  resolved boolean not null default false
);
create index if not exists idx_alerts_user_created on alerts(user_id, created_at desc);

-- PRICE HISTORY
create table if not exists price_history (
  id uuid primary key,
  product_id uuid not null references products(id) on delete cascade,
  price numeric(12,2) not null,
  registered_at timestamptz not null default now()
);
create index if not exists idx_price_product_time on price_history(product_id, registered_at desc);

-- PRODUCT RATING
create table if not exists product_ratings (
  id uuid primary key,
  product_id uuid not null references products(id) on delete cascade,
  score int not null check (score between 1 and 5),
  comment varchar(300),
  created_at timestamptz not null default now()
);
create index if not exists idx_rating_product_time on product_ratings(product_id, created_at desc);
