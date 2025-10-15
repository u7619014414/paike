FROM node:20.19.5

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apt-get update \
    && apt-get install -y git curl vim time net-tools \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

EXPOSE 45100

CMD ["tail", "-f", "/dev/null"]
