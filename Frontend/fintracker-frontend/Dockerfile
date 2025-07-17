# Dockerfile
FROM node:20.19-alpine as build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

# Use production .env file
ENV NODE_ENV=production
RUN npm run build

# Serve with a lightweight server
FROM nginx:stable-alpine as production
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
