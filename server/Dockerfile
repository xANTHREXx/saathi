FROM node:20-slim
WORKDIR /srv
COPY package*.json ./
RUN npm ci --omit=dev
COPY . .
ENV PORT=8080
CMD ["node", "index.js"]
