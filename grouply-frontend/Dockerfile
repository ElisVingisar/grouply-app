# ============================================
# BUILD
# ============================================
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

COPY .env.docker .env

RUN npm run build

# ============================================
# NGINX SERVER (RUNTIME)
# ============================================
FROM nginx:alpine

# ============================================
# Copy built files to Nginx
# ============================================
COPY --from=build /app/dist /usr/share/nginx/html

# ============================================
# Nginx configuration (SPA support)
# ============================================
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]