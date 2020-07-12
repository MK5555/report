package main

import (
	"log"
	"sort"
	"strconv"
	"strings"
	"sync"
)

//нужна функция которая сможет обрабатывать наши работы

func ExecutePipeline(jobs ...job) {

	//сделаем наши каналы
	in := make(chan interface{}, 100)
	out := make(chan interface{}, 100)

	//create sync
	wg := &sync.WaitGroup{}
	for _, job := range jobs {
		wg.Add(1)
		go worker(wg, job, in, out)

		in = out
		out = make(chan interface{}, 100)
	}

	wg.Wait()

}

func worker(wg *sync.WaitGroup, j job, in, out chan interface{}) {
	//
	defer wg.Done()
	defer close(out)
	j(in, out)
}

//first func for hash compute
func SingleHash(in, out chan interface{}) {

	var mu sync.Mutex
	outerWG := &sync.WaitGroup{}

	for input := range in {
		outerWG.Add(1)
		go func(in interface{}) {
			defer outerWG.Done()

			//когда использовать панику а когда фатал
			//проверка корректности входных значений
			value, ok := in.(int)
			if !ok {
				log.Fatal("err type")
			}
			data := strconv.Itoa(value)

			//при работе с картами нужно делать блокировку
			mu.Lock()
			hashMD5 := DataSignerMd5(data)
			mu.Unlock()

			mapWithHashData := map[string]string{
				"data":    data,
				"hashMD5": hashMD5,
			}

			mapForResultHash := make(map[string]string, 2)
			wg := &sync.WaitGroup{}
			for k1 := range mapWithHashData {
				wg.Add(1)
				go func(k string) {
					defer wg.Done()
					hash := DataSignerCrc32(mapWithHashData[k])
					mu.Lock()
					mapForResultHash[k] = hash
					mu.Unlock()
				}(k1)
			}
			wg.Wait()

			result := mapForResultHash["data"] + "~" + mapForResultHash["hashMD5"]
			out <- result
		}(input)
	}
	outerWG.Wait()
}

func MultiHash(in, out chan interface{}) {
	outerWG := &sync.WaitGroup{}
	for input := range in {
		outerWG.Add(1)
		go func(in interface{}) {
			defer outerWG.Done()
			data, ok := in.(string)
			if !ok {
				log.Fatal("M err")
			}
			wg := &sync.WaitGroup{}
			mu := &sync.Mutex{}

			mapForHash := make(map[int]string, 6)
			for t := 0; t < 6; t++ {
				wg.Add(1)
				go func(mapForHash map[int]string, t int, data string) {
					defer wg.Done()
					hash := DataSignerCrc32(strconv.Itoa(t) + data)
					//Блокировка нужна для того, чтобы
					mu.Lock()
					mapForHash[t] = hash
					mu.Unlock()
				}(mapForHash, t, data)
			}
			wg.Wait()

			keys := make([]int, 0, len(mapForHash))
			for key, _ := range mapForHash {
				keys = append(keys, key)
			}
			sort.Ints(keys)
			var resHash string
			for key := range keys {
				resHash += mapForHash[key]
			}

			out <- resHash

		}(input)
	}
	outerWG.Wait()
}

func CombineResults(in, out chan interface{}) {
	var hashes []string
	for input := range in {
		data, ok := input.(string)
		if !ok {
			log.Fatal("err CR")
		}
		hashes = append(hashes, data)
	}
	sort.Strings(hashes)
	finalResult := strings.Join(hashes, "_")
	out <- finalResult
}
