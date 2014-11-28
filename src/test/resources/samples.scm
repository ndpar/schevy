(begin

  (define append
    (lambda (x y)
      (if (null? x)
          y
          (cons (car x) (append (cdr x) y)))))

  (define factorial
    (lambda (n)
      (if (= n 1)
          1
          (* (factorial (- n 1)) n))))

  (assert (= (factorial 6) 720))

  (define factorial
    (lambda (n)
      (define iter
        (lambda (prod count)
          (if (< n count)
              prod
              (iter (* prod count)
                    (+ count 1)))))
      (iter 1 1)))

  (assert (= (factorial 6) 720)))
