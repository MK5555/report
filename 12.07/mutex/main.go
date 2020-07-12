package main

import "fmt"

func main(){
	//суть задания в том, что создаем пул воркеров, которые могут
	//выполнять какие-либо задания
	//для конкретно нашего случая есть вычисление хэша мультихеша и объединение хешей
	//commnon go дан по умолчанию

	//в нашем случае job может быть чем угодно
	//на вход поступает канал интерфейс на выход также
	var testResult string
	inputData := []int{123}

	hashSignJobs := []job{
		job(func(in, out chan interface{}) {
			for _, fibNum := range inputData {
				out <- fibNum
			}
		}),
		job(SingleHash),
		job(MultiHash),
		job(CombineResults),
		job(func(in, out chan interface{}) {
			dataRaw := <-in
			data, ok := dataRaw.(string)
			if !ok {
				panic("cant convert result data to string")
			}
			testResult = data
		}),
	}


	ExecutePipeline(hashSignJobs...)
	//end := time.Now().Sub(start)

	fmt.Println(testResult)

}