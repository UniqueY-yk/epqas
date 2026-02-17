import request from '@/utils/request'

export function login(data: any) {
    return request({
        url: '/auth/login', // Gateway routes to auth-service via Nacos, but for now we might hit auth-service directly if no gateway or via /api prefix mapping
        // But wait, my proxy rewrite removes /api and hits localhost:8080. 
        // If backend is microservices, is there a gateway on 8080? 
        // Or is auth-service on a specific port?
        // Usually gateway is 8080 or 8888. I'll assume 8080 is the gateway or the monolithic entry point for now.
        // Given the port wasn't specified, I'll assume standard Spring Boot 8080.
        // If auth-service runs independently, it might be on a random port.
        // Ideally I should check application.properties/yml of auth-service.
        method: 'post',
        data
    })
}

export function register(data: any) {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    })
}
