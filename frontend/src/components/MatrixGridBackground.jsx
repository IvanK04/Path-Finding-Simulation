import React, { useEffect, useRef } from 'react';
import './MatrixGridBackground.css';

export default function MatrixGridBackground() {
    const containerRef = useRef(null);

    useEffect(() => {
        const container = containerRef.current;
        if (!container) return;

        // 1. Tính toán số lượng ô vuông cần thiết để phủ kín mọi độ phân giải màn hình
        const cellSize = 30; // Kích thước ô vuông (khớp với trang mô phỏng)
        const columns = Math.ceil(window.innerWidth / cellSize) + 1;
        const rows = Math.ceil(window.innerHeight / cellSize) + 1;
        const totalCells = columns * rows;

        // 2. Tạo nhanh chuỗi HTML chứa hàng ngàn ô cell trống
        let cellsHTML = '';
        for (let i = 0; i < totalCells; i++) {
            cellsHTML += `<div class="bg-cell"></div>`;
        }
        container.innerHTML = cellsHTML;

        // 3. Thiết lập bộ đếm thời gian (Interval) để đổi màu ngẫu nhiên
        const allCells = container.getElementsByClassName('bg-cell');

        const intervalId = setInterval(() => {
            // Mỗi vòng chạy (ví dụ 100ms), chọn ngẫu nhiên khoảng 15-20 ô để kích hoạt hiệu ứng
            for (let k = 0; k < 20; k++) {
                const randomIndex = Math.floor(Math.random() * allCells.length);
                const cell = allCells[randomIndex];

                if (cell && !cell.classList.contains('blink-active')) {
                    cell.classList.add('blink-active');

                    // Sau khi chạy xong hiệu ứng CSS Animation (1.5 giây), tự động xóa class để ô trở lại màu trắng gốc
                    setTimeout(() => {
                        cell.classList.remove('blink-active');
                    }, 1500);
                }
            }
        }, 100);

        return () => clearInterval(intervalId);
    }, []);

    return <div className="matrix-bg-container" ref={containerRef} />;
}