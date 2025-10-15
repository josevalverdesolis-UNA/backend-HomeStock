package cr.ac.una.homestock.web

import cr.ac.una.homestock.dto.*
import cr.ac.una.homestock.service.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

// Nota: agrega en application.yml
// url:
//   v1: /v1
//   categories: ${url.v1}/categories
//   products: ${url.v1}/products
//   purchases: ${url.v1}/purchases
//   shopping: ${url.v1}/shopping-items
//   users: ${url.v1}/users

@RestController
@RequestMapping("\${url.categories}")
class CategoryControllerV2(private val svc: CategoryService) {
    @GetMapping fun list() = svc.list()
    @GetMapping("/{id}") fun get(@PathVariable id: String) = svc.get(id)
    @PostMapping @ResponseStatus(HttpStatus.CREATED) fun create(@RequestBody @Valid input: CategoryInput) = svc.create(input)
    @PatchMapping("/{id}") fun patch(@PathVariable id: String, @RequestBody input: CategoryInput) = svc.update(id, input)
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) fun delete(@PathVariable id: String) = svc.delete(id)
}

@RestController
@RequestMapping("\${url.products}")
class ProductControllerV2(private val svc: ProductService) {
    @GetMapping fun list(@RequestParam(required = false) userId: String?) = svc.list(userId)
    @GetMapping("/{id}") fun get(@PathVariable id: String) = svc.get(id)
    @PostMapping @ResponseStatus(HttpStatus.CREATED) fun create(@RequestBody @Valid input: ProductInput) = svc.create(input)
    @PatchMapping("/{id}") fun patch(@PathVariable id: String, @RequestBody input: ProductInput) = svc.update(id, input)
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) fun delete(@PathVariable id: String) = svc.delete(id)
}

@RestController
@RequestMapping("\${url.purchases}")
class PurchaseControllerV2(private val svc: PurchaseService) {
    @GetMapping fun list(@RequestParam(required = false) userId: String?) = svc.list(userId)
    @GetMapping("/{id}") fun get(@PathVariable id: String) = svc.get(id)
    @PostMapping @ResponseStatus(HttpStatus.CREATED) fun create(@RequestBody @Valid input: PurchaseInput) = svc.create(input)
    @PatchMapping("/{id}") fun patch(@PathVariable id: String, @RequestBody input: PurchaseInput) = svc.update(id, input)
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) fun delete(@PathVariable id: String) = svc.delete(id)
}

@RestController
@RequestMapping("\${url.shopping}")
class ShoppingItemControllerV2(private val svc: ShoppingItemService) {
    @GetMapping fun list(@RequestParam(required = false) userId: String?, @RequestParam(required = false) purchased: Boolean?) = svc.list(userId, purchased)
    @GetMapping("/{id}") fun get(@PathVariable id: String) = svc.get(id)
    @PostMapping @ResponseStatus(HttpStatus.CREATED) fun create(@RequestBody @Valid input: ShoppingItemInput) = svc.create(input)
    @PatchMapping("/{id}") fun patch(@PathVariable id: String, @RequestBody input: ShoppingItemInput) = svc.update(id, input)
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) fun delete(@PathVariable id: String) = svc.delete(id)
}

@RestController
@RequestMapping("\${url.users}")
class UserControllerV2(private val svc: UserService) {
    @GetMapping fun list() = svc.list()
    @GetMapping("/{id}") fun get(@PathVariable id: String) = svc.get(id)
    @PostMapping @ResponseStatus(HttpStatus.CREATED) fun create(@RequestBody @Valid input: UserInput) = svc.create(input)
    @PatchMapping("/{id}") fun patch(@PathVariable id: String, @RequestBody input: UserInput) = svc.update(id, input)
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) fun delete(@PathVariable id: String) = svc.delete(id)
}
