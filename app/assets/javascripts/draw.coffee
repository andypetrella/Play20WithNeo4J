
$ ->
    Renderer = (canvas) ->
      canvas = $(canvas).get(0)
      ctx = canvas.getContext("2d");
      particleSystem = undefined
      that = {
        init: (system) =>
          particleSystem = system

          particleSystem.screenSize(canvas.width, canvas.height)
          particleSystem.screenPadding(80)

          that.initMouseHandling()

        redraw: () =>
          ctx.fillStyle = "white"
          ctx.fillRect(0,0, canvas.width, canvas.height)

          particleSystem.eachEdge((edge, pt1, pt2) =>
            if (edge.data && edge.data.type && edge.data.type is "knows")
              ctx.strokeStyle = "rgba(255,0,0, .666)"
            else
              ctx.strokeStyle = "rgba(0,0,0, .333)"

            ctx.lineWidth = 1
            ctx.beginPath()
            ctx.moveTo(pt1.x, pt1.y)
            ctx.lineTo(pt2.x, pt2.y)
            ctx.stroke()
          )

          particleSystem.eachNode((node, pt) =>
            w = 20
            if (node.data.root)
              ctx.beginPath()
              ctx.arc(pt.x, pt.y, w/2, 0, 2 * Math.PI, false)
              ctx.fillStyle = "#8ED6FF"
              ctx.lineWidth = 5
              ctx.strokeStyle = "black";
              ctx.fill()
              ctx.stroke()
            else
              ctx.beginPath()
              ctx.moveTo(pt.x, pt.y-w/2)
              ctx.lineTo(pt.x+w/2, pt.y+w/2)
              ctx.lineTo(pt.x-w/2, pt.y+w/2)
              ctx.lineTo(pt.x, pt.y-w/2)
              ctx.fillStyle = "blue"
              if (node.data.enabledGroup)
                ctx.lineWidth = 3
                ctx.strokeStyle = "red";
              else
                ctx.lineWidth = 1
                ctx.strokeStyle = "black";
              ctx.fill()
              ctx.stroke()

              ctx.beginPath()
              ctx.arc(pt.x, pt.y-w/2, w/2, 2 * Math.PI, false)

              ctx.fillStyle = "blue"
              if (node.data.enabledGroup)
                ctx.lineWidth = 3
                ctx.strokeStyle = "red";
              else
                ctx.lineWidth = 1
                ctx.strokeStyle = "black";
              ctx.fill()
              ctx.stroke()

              ctx.fillText(node.data.firstName, pt.x, pt.y+w);
          )

        initMouseHandling: () =>
          $(canvas).bind "mousedown", (e) =>
            pos = $(canvas).offset();
            _mouseP = arbor.Point(e.pageX-pos.left, e.pageY-pos.top)
            node = particleSystem.nearest(_mouseP)
            node = node.node
            $.get(
                playRoutes.controllers.Users.j_knows(node.data.id).url
                (knows) =>
                  particleSystem.eachNode((n, pt) =>
                    particleSystem.addEdge(node, n, {type:"knows"}) for k in knows when (n.data.id is k.id)
                  )
            )

      }


    users = {}

    drawUsers = () =>
      nodes = []

      sys = arbor.ParticleSystem(1000, 600, 0.5) #// create the system with sensible repulsion/stiffness/friction
      sys.parameters({gravity:true}) #// use center-gravity to make the graph settle nicely (ymmv)
      sys.renderer = Renderer("#viewport") #// our newly created renderer will have its .init() method called shortly by sys...

      nodes.push(sys.addNode("user-"+k, u)) for k,u of users

      root = sys.addNode("root", {root:true})
      sys.addEdge(root, n) for n in nodes

      $.get(
          playRoutes.controllers.Groups.j_all().url
          (gs) ->
            $("#groups").append($("<option value='"+ g.id + "'>" + g.name + "</option>")) for g in gs
      )

      $("#groups").bind "change", (event) =>
        groupId = $("#groups option:selected").val()
        if (groupId isnt -1)
          $.get(
            playRoutes.controllers.Groups.j_users(groupId).url
            (us) ->
              sys.eachNode((n, pt) =>
                ok = false
                for u in us
                  if (u.id is n.data.id)
                    ok = true
                    break
                n.data.enabledGroup = ok
              )
              sys.renderer.redraw()
          )


    routeToAllUsers = playRoutes.controllers.Users.j_all().url
    $.get(
        routeToAllUsers
        (us) ->
          users[u.id] = u for u in us
          drawUsers()
    )

